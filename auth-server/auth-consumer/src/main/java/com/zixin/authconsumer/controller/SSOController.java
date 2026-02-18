package com.zixin.authconsumer.controller;

import com.zixin.accountapi.dto.LoginRequest;
import com.zixin.accountapi.dto.RegisterRequest;
import com.zixin.authconsumer.client.AuthClient;
import com.zixin.authconsumer.service.SSOServiceImpl;
import com.zixin.thirdpartyapi.dto.SendSMSRequest;
import com.zixin.utils.exception.ToBCodeEnum;
import com.zixin.utils.utils.Result;
import dto.RefreshTokenRequest;
import dto.RefreshTokenResponse;
import dto.ValidateTokenRequest;
import dto.ValidateTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * SSO认证控制器
 * 提供登录、注册、Token刷新、Token验证等接口
 * 
 * 注意: 这些接口应该配置在Gateway的白名单中，不需要Token验证
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SSOController {

    private final SSOServiceImpl ssoService;
    private final AuthClient authClient;

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求(包含账号和密码)
     * @return 包含Token的登录结果
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest loginRequest) {
        log.info("Login request received for account: {}", loginRequest.getLoginAccount());
        return ssoService.login(loginRequest);
    }

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterRequest registerRequest) {
        log.info("Register request received for account: {}", registerRequest.getUsername());
        return ssoService.register(registerRequest);
    }

    /**
     * 发送短信验证码
     * 
     * @param sendSMSRequest 短信发送请求
     * @return 发送结果
     */
    @PostMapping("/sms/code")
    public Result sendSmsCode(@RequestBody SendSMSRequest sendSMSRequest) {
        log.info("SMS code request received for phone: {}", sendSMSRequest.getPhone());
        return ssoService.sendCode(sendSMSRequest);
    }

    /**
     * 刷新Token
     * 使用Refresh Token获取新的Token对
     * 
     * @param request 包含Refresh Token的请求
     * @return 新的Token对
     */
    @PostMapping("/refresh")
    public Result refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        
        RefreshTokenResponse response = authClient.refreshToken(request);
        
        if (response.getCode() == ToBCodeEnum.SUCCESS) {
            return Result.success(response);
        } else {
            log.warn("Token refresh failed: {}", response.getMessage());
            return Result.error(response.getMessage());
        }
    }

    /**
     * 验证Token
     * 验证Token是否有效，并返回用户信息
     * 
     * @param request 包含Token的请求
     * @return 验证结果和用户信息
     */
    @PostMapping("/validate")
    public Result validateToken(@RequestBody ValidateTokenRequest request) {
        log.info("Token validation request received");
        
        ValidateTokenResponse response = authClient.validateToken(request);
        
        if (response.getValid()) {
            return Result.success(response);
        } else {
            log.warn("Token validation failed: {}", response.getMessage());
            return Result.error(response.getMessage());
        }
    }

    /**
     * 登出
     * 撤销Token，将其加入黑名单
     * 
     * @param request 包含Token的请求
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Result logout(@RequestBody ValidateTokenRequest request) {
        log.info("Logout request received");
        
        ValidateTokenResponse response = authClient.revokeToken(request);
        
        if (response.getCode() == ToBCodeEnum.SUCCESS) {
            return Result.success("Logout successful");
        } else {
            log.warn("Logout failed: {}", response.getMessage());
            return Result.error(response.getMessage());
        }
    }
}
