package com.zixin.accountprovider.utils;

import com.zixin.accountapi.enums.Action;
import com.zixin.accountapi.enums.RoleCode;

import java.util.HashSet;
import java.util.Set;

/**
 * 权限展开工具类
 * 
 * 用于将角色+行为组合展开为具体的权限字符串
 * 
 * 当前格式: ROLE:ACTION (如 DOCTOR:READ, PATIENT:WRITE)
 * 建议格式: resource:operation (如 user:read, medical:record:write)
 * 
 * TODO: 未来可以扩展为更细粒度的权限管理
 */
public final class PermissionExpandUtil {

    private PermissionExpandUtil() {
        // 工具类禁止实例化
    }

    /**
     * 根据角色 + 行为，展开最终权限字符串
     *
     * @param roleCode   RoleCode.code (1-DOCTOR, 2-PATIENT, 3-FAMILY)
     * @param actionCode Action.code (1-READ, 2-WRITE, 3-ALL)
     * @return 权限字符串集合
     * 
     * 示例:
     * - expand(1, 3) → ["DOCTOR:READ", "DOCTOR:WRITE", "DOCTOR:ALL"]
     * - expand(2, 1) → ["PATIENT:READ"]
     */
    public static Set<String> expand(Integer roleCode, Integer actionCode) {

        if (roleCode == null || actionCode == null) {
            throw new IllegalArgumentException("roleCode or actionCode cannot be null");
        }

        RoleCode role = RoleCode.fromCode(roleCode);
        Action action = Action.fromCode(actionCode);

        Set<String> authorities = new HashSet<>();

        switch (action) {
            case ALL:
                // ALL权限包含READ、WRITE和ALL本身
                authorities.addAll(expandToResourcePermissions(role, Action.READ));
                authorities.addAll(expandToResourcePermissions(role, Action.WRITE));
                authorities.addAll(expandToResourcePermissions(role, Action.ALL));
                break;
            case READ:
            case WRITE:
                authorities.addAll(expandToResourcePermissions(role, action));
                break;
            default:
                throw new IllegalStateException("Unsupported action: " + action);
        }

        return authorities;
    }

    /**
     * 将角色+行为展开为资源级别的权限
     * 
     * 这里可以配置每个角色+行为对应的具体资源权限
     * 当前使用简单的ROLE:ACTION格式,未来可以扩展为更细粒度的权限
     */
    private static Set<String> expandToResourcePermissions(RoleCode role, Action action) {
        Set<String> permissions = new HashSet<>();
        
        // 当前使用简单的ROLE:ACTION格式
        permissions.add(build(role, action));
        
        // TODO: 未来可以扩展为细粒度权限
        // 例如:
        // if (role == RoleCode.DOCTOR && action == Action.READ) {
        //     permissions.add("user:read");
        //     permissions.add("patient:read");
        //     permissions.add("medical:record:read");
        // } else if (role == RoleCode.DOCTOR && action == Action.WRITE) {
        //     permissions.add("user:write");
        //     permissions.add("medical:record:write");
        //     permissions.add("prescription:create");
        // }
        
        return permissions;
    }

    /**
     * 构造统一格式的权限字符串
     * 格式: ROLE:ACTION
     * 
     * 示例:
     * - DOCTOR:READ
     * - PATIENT:WRITE
     * - FAMILY:ALL
     */
    private static String build(RoleCode role, Action action) {
        return role.name() + ":" + action.name();
    }
}
