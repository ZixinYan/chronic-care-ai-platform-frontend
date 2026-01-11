package com.zixin.accountprovider.utils;

import com.zixin.accountapi.enums.Action;
import com.zixin.accountapi.enums.RoleCode;

import java.util.HashSet;
import java.util.Set;

public final class PermissionExpandUtil {

    private PermissionExpandUtil() {
        // 工具类禁止实例化
    }

    /**
     * 根据角色 + 行为，展开最终权限字符串
     *
     * @param roleCode   RoleCode.code
     * @param actionCode Action.code
     * @return 权限字符串集合，如 DOCTOR:READ
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
                authorities.add(build(role, Action.READ));
                authorities.add(build(role, Action.WRITE));
                authorities.add(build(role, Action.ALL));
                break;
            case READ:
            case WRITE:
                authorities.add(build(role, action));
                break;
            default:
                throw new IllegalStateException("Unsupported action: " + action);
        }

        return authorities;
    }

    /**
     * 构造统一格式的权限字符串
     */
    private static String build(RoleCode role, Action action) {
        return role.name() + ":" + action.name();
    }
}
