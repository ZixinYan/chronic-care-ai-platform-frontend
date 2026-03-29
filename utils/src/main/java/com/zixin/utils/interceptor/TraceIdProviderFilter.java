package com.zixin.utils.interceptor;

import com.zixin.utils.constant.HeaderConstant;
import com.zixin.utils.context.UserInfoContext;
import com.zixin.utils.context.UserInfoManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.MDC;

/**
 * Dubbo提供者拦截器 - 从RPC上下文恢复用户信息和TraceId到ThreadLocal
 */
@Slf4j
@Activate(group = {CommonConstants.PROVIDER}, order = -1000)
public class TraceIdProviderFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        boolean traceIdSet = false;

        try {
            // 1. 优先从RPC上下文获取TraceId并设置到MDC
            String traceId = RpcContext.getServerAttachment().getAttachment(HeaderConstant.TRACE_ID);

            if (traceId != null && !traceId.isEmpty()) {
                MDC.put(HeaderConstant.TRACE_ID, traceId);
                traceIdSet = true;
                log.debug("RPC服务端设置TraceId到MDC: {}", traceId);
            }

            // 2. 从RPC隐式参数获取用户信息
            UserInfoContext context = extractUserInfoFromRpcContext();

            if (context != null) {
                // 如果用户信息中没有TraceId，但RPC上下文有，补充进去
                if (context.getTraceId() == null && traceId != null) {
                    context.setTraceId(traceId);
                }

                // 设置到ThreadLocal
                UserInfoManager.setUserContext(context);

                log.debug("RPC服务端恢复用户信息 - userId: {}, username: {}, traceId: {}",
                        context.getUserId(), context.getUsername(),
                        MDC.get(HeaderConstant.TRACE_ID));
            }
        } catch (Exception e) {
            log.error("RPC服务端恢复用户信息失败", e);
        }

        try {
            return invoker.invoke(invocation);
        } finally {
            // 清理ThreadLocal和MDC
            UserInfoManager.clearUserContext();
            if (traceIdSet) {
                MDC.remove(HeaderConstant.TRACE_ID);
            }
        }
    }

    /**
     * 从RPC上下文提取用户信息
     */
    private UserInfoContext extractUserInfoFromRpcContext() {
        String userId = RpcContext.getServerAttachment().getAttachment(HeaderConstant.USER_ID);
        String username = RpcContext.getServerAttachment().getAttachment(HeaderConstant.USERNAME);

        // 如果没有用户信息，返回null
        if (userId == null && username == null) {
            return null;
        }

        return UserInfoContext.builder()
                .traceId(RpcContext.getServerAttachment().getAttachment(HeaderConstant.TRACE_ID))
                .userId(parseLong(userId))
                .username(username)
                .userType(parseInt(RpcContext.getServerAttachment().getAttachment(HeaderConstant.USER_TYPE)))
                .roles(RpcContext.getServerAttachment().getAttachment(HeaderConstant.USER_ROLES))
                .authorities(RpcContext.getServerAttachment().getAttachment(HeaderConstant.USER_AUTHORITIES))
                .nickname(RpcContext.getServerAttachment().getAttachment(HeaderConstant.NICKNAME))
                .phone(RpcContext.getServerAttachment().getAttachment(HeaderConstant.PHONE))
                .email(RpcContext.getServerAttachment().getAttachment(HeaderConstant.EMAIL))
                .attendingDoctorId(parseLong(RpcContext.getServerAttachment().getAttachment(HeaderConstant.ATTENDING_DOCTOR_ID)))
                .departmentId(parseLong(RpcContext.getServerAttachment().getAttachment(HeaderConstant.DEPARTMENT_ID)))
                .requestTime(System.currentTimeMillis())
                .build();
    }

    private Long parseLong(String str) {
        try {
            return str != null && !str.isEmpty() ? Long.parseLong(str) : null;
        } catch (NumberFormatException e) {
            log.warn("解析Long失败: {}", str);
            return null;
        }
    }

    private Integer parseInt(String str) {
        try {
            return str != null && !str.isEmpty() ? Integer.parseInt(str) : null;
        } catch (NumberFormatException e) {
            log.warn("解析Integer失败: {}", str);
            return null;
        }
    }
}