package com.zixin.accountprovider.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zixin.accountapi.api.AccountManagementAPI;
import com.zixin.accountapi.dto.*;
import com.zixin.accountapi.enums.Action;
import com.zixin.accountapi.enums.RoleCode;
import com.zixin.accountapi.po.Account;
import com.zixin.accountapi.po.AccountRole;
import com.zixin.accountapi.po.RolePermission;
import com.zixin.accountprovider.config.RoleConfig;
import com.zixin.accountprovider.mapper.AccountMapper;
import com.zixin.accountprovider.mapper.AccountRoleMapper;
import com.zixin.accountprovider.mapper.RolePermissionMapper;
import com.zixin.accountprovider.utils.AccountUtils;
import com.zixin.utils.exception.ToBCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@DubboService
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountManagementAPI {

    private final AccountRoleMapper accountRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    
    @Autowired
    private RoleConfig roleConfig;

    public AccountServiceImpl(AccountRoleMapper accountRoleMapper, RolePermissionMapper rolePermissionMapper) {
        this.accountRoleMapper = accountRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }


    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String loginAccount = loginRequest.getLoginAccount();
        String password = loginRequest.getPassword();
        LoginResponse loginResponse = new LoginResponse();

        // 1. 校验请求参数
        if (loginAccount == null || loginAccount.isEmpty() || password == null || password.isEmpty()) {
            loginResponse.setCode(ToBCodeEnum.FAIL);
            loginResponse.setMessage("Login account or password cannot be empty");
            return loginResponse;
        }

        Account account;
        try {
            // 2. 根据用户名/手机号哈希/身份证号哈希查找账号
            // 生成哈希值用于查询（手机号和身份证号已加密，需要通过哈希值查询）
            String accountHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(loginAccount);
            
            account = this.baseMapper.selectOne(new QueryWrapper<Account>()
                    .eq("username", loginAccount)           // 用户名查询
                    .or().eq("phone_hash", accountHash)     // 手机号哈希查询
                    .or().eq("id_card_hash", accountHash)); // 身份证号哈希查询
        } catch (Exception e) {
            log.error("Fail to query account by loginAccount:{}, error:{}", loginAccount, e.getMessage());
            loginResponse.setCode(ToBCodeEnum.FAIL);
            loginResponse.setMessage("Database query failed: " + e.getMessage());
            return loginResponse;
        }

        if (account == null) {
            log.info("Fail to login, account not found: {}", loginAccount);
            loginResponse.setCode(ToBCodeEnum.FAIL);
            loginResponse.setMessage("Account not found");
            return loginResponse;
        }

        // 3. 校验密码
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(password, account.getPassword())) {
            log.info("Password is wrong, account: {}", loginAccount);
            loginResponse.setCode(ToBCodeEnum.FAIL);
            loginResponse.setMessage("Incorrect password");
            return loginResponse;
        }

        // 4. 查询用户角色和权限
        List<Integer> roleCodes = accountRoleMapper.selectRoleCodesByUserId(account.getAccountId());
        // roleCodes 示例：[1, 2] 对应 DOCTOR, PATIENT

        Set<String> permissions = new HashSet<>();
        if (!roleCodes.isEmpty()) {
            // 查询角色对应的权限
            List<RolePermission> rolePermissions = rolePermissionMapper.selectByRoleCodes(roleCodes);
            for (RolePermission rp : rolePermissions) {
                // 将 roleCode + action 转换为权限字符串，例如 DOCTOR:READ
                String roleName = RoleCode.fromCode(rp.getRoleCode()).name();
                String actionName = Action.fromCode(rp.getActionCode()).name();
                permissions.add(roleName + ":" + actionName);
            }
        }

        // 5. 封装返回用户信息
        LoginResponse.LoginUserDTO userDTO = new LoginResponse.LoginUserDTO(
                account.getAccountId(),
                account.getUsername(),
                account.getNickname(),
                account.getEmail(),
                account.getGender(),
                account.getAvatarUrl(),
                account.getAddress(),
                account.getBirthday(),
                account.getIdCard(),
                roleCodes,
                permissions,
                account.getExt()
        );


        loginResponse.setData(userDTO);
        loginResponse.setCode(ToBCodeEnum.SUCCESS);
        loginResponse.setMessage("Login successful");
        log.info("Login successful: {}", loginAccount);

        return loginResponse;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(RegisterRequest registerRequest) {
        Account account = new Account();
        RegisterResponse registerResponse = new RegisterResponse();
        
        // 赋值基础信息
        account.setUsername(registerRequest.getUsername());
        account.setNickname(registerRequest.getNickname());
        account.setPhone(registerRequest.getPhone());
        account.setAddress(registerRequest.getAddress());
        account.setBirthday(registerRequest.getBirthday());
        account.setGender(registerRequest.getGender());
        account.setIdCard(registerRequest.getIdCard());
        
        // 生成手机号哈希值（用于登录查询）
        if (registerRequest.getPhone() != null && !registerRequest.getPhone().trim().isEmpty()) {
            account.setPhoneHash(org.apache.commons.codec.digest.DigestUtils.sha256Hex(registerRequest.getPhone()));
        }
        
        // 生成身份证号哈希值（用于登录查询）
        if (registerRequest.getIdCard() != null && !registerRequest.getIdCard().trim().isEmpty()) {
            account.setIdCardHash(org.apache.commons.codec.digest.DigestUtils.sha256Hex(registerRequest.getIdCard()));
        }
        
        // 加密密码
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        account.setPassword(encodedPassword);
        
        // 防重校验并插入账户
        try{
            this.baseMapper.insert(account);
        }catch (DuplicateKeyException e){
            log.error("fail to register account, repeated account: {}", account);
            registerResponse.setCode(ToBCodeEnum.FAIL);
            registerResponse.setMessage("用户名、手机号或身份证已存在");
            return registerResponse;
        }catch (Exception e){
            log.error("fail to register account, db wrong: {}", account);
            registerResponse.setCode(ToBCodeEnum.FAIL);
            registerResponse.setMessage(e.getMessage());
            return registerResponse;
        }
        
        // 分配角色
        List<Integer> roleCodes = registerRequest.getRoleCodes();
        if (roleCodes == null || roleCodes.isEmpty()) {
            // 使用默认角色
            roleCodes = roleConfig.getDefaultRoles();
            log.info("User {} registered with default roles: {}", account.getUsername(), roleCodes);
        } else {
            log.info("User {} registered with specified roles: {}", account.getUsername(), roleCodes);
        }
        
        // 插入用户角色关系
        if (!roleCodes.isEmpty()) {
            try {
                for (Integer roleCode : roleCodes) {
                    AccountRole accountRole = new AccountRole();
                    accountRole.setUserId(account.getAccountId());
                    accountRole.setRoleCode(roleCode);
                    accountRole.setCreateTime(new Date());
                    accountRoleMapper.insert(accountRole);
                }
            } catch (Exception e) {
                log.error("fail to assign roles to account, accountId: {}, error: {}", 
                        account.getAccountId(), e.getMessage());
                // 事务会回滚
                throw new RuntimeException("分配角色失败: " + e.getMessage());
            }
        }
        
        registerResponse.setCode(ToBCodeEnum.SUCCESS);
        registerResponse.setMessage("注册成功");
        return registerResponse;
    }

    @Override
    public GetUserInfoResponse getUserInfo(GetUserInfoRequest getUserInfoRequest) {
        GetUserInfoResponse getUserInfoResponse = new GetUserInfoResponse();
        String join = StrUtil.join(",", getUserInfoRequest.getUserIds());
        List<GetUserInfoResponse.UserInfoDTO> userInfoDTOList;
        try {
             userInfoDTOList = lambdaQuery()
                    .in(Account::getAccountId, getUserInfoRequest)
                    .last("order by field(id, " + join + ")")
                    .list()
                    .stream().map(account ->
                            BeanUtil.copyProperties(account, GetUserInfoResponse.UserInfoDTO.class))
                    .collect(Collectors.toList());
        }catch (Exception e){
            log.error("fail to get user info, error:{}",e.getMessage());
            getUserInfoResponse.setCode(ToBCodeEnum.FAIL);
            getUserInfoResponse.setMessage(e.getMessage());
            return getUserInfoResponse;
        }
        getUserInfoResponse.setCode(ToBCodeEnum.SUCCESS);
        getUserInfoResponse.setUsers(userInfoDTOList);
        return getUserInfoResponse;
    }

    @Override
    public UpdateUserInfoResponse updateUserInfo(UpdateUserInfoRequest updateUserInfoRequest) {
        Long accountId = Long.valueOf(updateUserInfoRequest.getUpdateData().get("account_id").toString());
        Account account = new Account();

        UpdateUserInfoResponse updateUserInfoResponse = new UpdateUserInfoResponse();
        if(AccountUtils.validateUpdateData(updateUserInfoRequest.getUpdateData())){
            log.error("illegal update data,{}", updateUserInfoRequest.getUpdateData());
            updateUserInfoResponse.setCode(ToBCodeEnum.FAIL);
            updateUserInfoResponse.setMessage("illegal update data");
            return updateUserInfoResponse;
        }
        try {
            account = this.baseMapper.selectById(accountId);
        }catch (Exception e){
            log.error("fail to get account, error:{}",e.getMessage());
            updateUserInfoResponse.setCode(ToBCodeEnum.FAIL);
            updateUserInfoResponse.setMessage(e.getMessage());
            return updateUserInfoResponse;
        }

        if (account == null) {
            log.error("fail to update user info, account is null");
            updateUserInfoResponse.setCode(ToBCodeEnum.FAIL);
            updateUserInfoResponse.setMessage("account is null");
            return updateUserInfoResponse;
        }
        
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("account_id", accountId);
        updateUserInfoRequest.getUpdateData().forEach((key,value) -> {
            if(value != null) {
                updateWrapper.set(key, value);
                
                // 如果更新的是手机号，同时更新手机号哈希
                if("phone".equals(key)) {
                    String phoneHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(value.toString());
                    updateWrapper.set("phone_hash", phoneHash);
                }
                
                // 如果更新的是身份证号，同时更新身份证号哈希
                if("id_card".equals(key)) {
                    String idCardHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(value.toString());
                    updateWrapper.set("id_card_hash", idCardHash);
                }
            }
        });
        updateWrapper.set("update_time", new Date());

        try{
            boolean check = update(updateWrapper);
            if (!check) {
                updateUserInfoResponse.setCode(ToBCodeEnum.FAIL);
                updateUserInfoResponse.setMessage("account update failed");
                return updateUserInfoResponse;
            }
        }catch (Exception e){
            log.error("fail to update user info, error:{}",e.getMessage());
            updateUserInfoResponse.setCode(ToBCodeEnum.FAIL);
            updateUserInfoResponse.setMessage(e.getMessage());
            return updateUserInfoResponse;
        }
        updateUserInfoResponse.setCode(ToBCodeEnum.SUCCESS);
        return updateUserInfoResponse;
    }

    @Override
    public UpdatePasswordResponse updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        Long userId = updatePasswordRequest.getUserId();
        String oldPassword = updatePasswordRequest.getOldPassword();
        Account account = new Account();
        UpdatePasswordResponse updatePasswordResponse = new UpdatePasswordResponse();
        try {
            account = this.baseMapper.selectById(userId);
            if (!oldPassword.equals(account.getPassword())) {
                log.error("fail to update password, old password is wrong");
                updatePasswordResponse.setCode(ToBCodeEnum.FAIL);
                updatePasswordResponse.setMessage("old password is wrong");
                return updatePasswordResponse;
            }
            account.setPassword(oldPassword);
            this.baseMapper.updateById(account);
        }catch (Exception e){
            log.error("fail to update password, error:{}",e.getMessage());
            updatePasswordResponse.setCode(ToBCodeEnum.FAIL);
            updatePasswordResponse.setMessage(e.getMessage());
            return updatePasswordResponse;
        }
        updatePasswordResponse.setCode(ToBCodeEnum.SUCCESS);
        return updatePasswordResponse;
    }

    @Override
    public DeleteUserResponse deleteUser(DeleteUserRequest deleteUserRequest) {
        DeleteUserResponse deleteUserResponse = new DeleteUserResponse();
        try {
            removeByIds(Arrays.asList(deleteUserRequest.getUserId()));
        }catch (Exception e){
            log.error("fail to delete user, error:{}",e.getMessage());
            deleteUserResponse.setCode(ToBCodeEnum.FAIL);
            deleteUserResponse.setMessage(e.getMessage());
            return deleteUserResponse;
        }
        deleteUserResponse.setCode(ToBCodeEnum.SUCCESS);
        return deleteUserResponse;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UpdateUserRolesResponse updateUserRoles(UpdateUserRolesRequest request) {
        UpdateUserRolesResponse response = new UpdateUserRolesResponse();
        
        try {
            Long accountId = request.getAccountId();
            List<Integer> newRoleCodes = request.getRoleCodes();
            
            if (accountId == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("账户ID不能为空");
                return response;
            }
            
            if (newRoleCodes == null || newRoleCodes.isEmpty()) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("角色列表不能为空");
                return response;
            }
            
            // 验证账户是否存在
            Account account = this.baseMapper.selectById(accountId);
            if (account == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("账户不存在");
                return response;
            }
            
            // 删除用户现有的所有角色
            accountRoleMapper.deleteByAccountId(accountId);
            
            // 插入新的角色
            for (Integer roleCode : newRoleCodes) {
                AccountRole accountRole = new AccountRole();
                accountRole.setUserId(accountId);
                accountRole.setRoleCode(roleCode);
                accountRole.setCreateTime(new Date());
                accountRoleMapper.insert(accountRole);
            }
            
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("更新角色成功");
            response.setRoleCodes(newRoleCodes);
            
            log.info("Update user roles success, accountId: {}, new roles: {}", accountId, newRoleCodes);
        } catch (Exception e) {
            log.error("Update user roles failed, error: {}", e.getMessage(), e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("更新角色失败: " + e.getMessage());
        }
        
        return response;
    }


}
