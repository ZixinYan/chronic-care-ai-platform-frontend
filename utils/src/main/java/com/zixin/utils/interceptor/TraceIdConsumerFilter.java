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
 * Dubbo消费者拦截器 - 将用户信息和TraceId传递到RPC上下文
 */
@Slf4j
@Activate(group = {CommonConstants.CONSUMER}, order = -1000)
public class TraceIdConsumerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try {
            // 从ThreadLocal获取用户信息
            UserInfoContext userContext = UserInfoManager.getUserContext();

            if (userContext != null) {
                log.debug("传递用户信息到RPC服务 - userId: {}, traceId: {}",
                        userContext.getUserId(), userContext.getTraceId());

                // 设置所有用户信息到RPC隐式参数
                setUserInfoAttachments(userContext);

                // 确保TraceId设置到RPC上下文
                String traceId = userContext.getTraceId();
                if (traceId == null || traceId.isEmpty()) {
                    traceId = MDC.get(HeaderConstant.TRACE_ID);
                }

                if (traceId != null && !traceId.isEmpty()) {
                    RpcContext.getClientAttachment().setAttachment(HeaderConstant.TRACE_ID, traceId);
                    log.debug("传递TraceId到RPC服务: {}", traceId);
                }
            } else {
                // 即使没有用户信息，也要尝试传递TraceId
                String traceId = MDC.get(HeaderConstant.TRACE_ID);
                if (traceId != null && !traceId.isEmpty()) {
                    RpcContext.getClientAttachment().setAttachment(HeaderConstant.TRACE_ID, traceId);
                    log.debug("仅传递TraceId到RPC服务: {}", traceId);
                }
            }
        } catch (Exception e) {
            log.error("传递用户信息到RPC失败", e);
        }

        return invoker.invoke(invocation);
    }

    /**
     * 设置用户信息到RPC附件
     */
    private void setUserInfoAttachments(UserInfoContext context) {
        if (context.getUserId() != null) {
            RpcContext.getClientAttachment().setAttachment(HeaderConstant.USER_ID,
                    context.getUserId().toString());
        }
        if (context.getUsername() != null) {
            RpcContext.getClientAttachment().setAttachment(HeaderConstant.USERNAME,
                    context.getUsername());
        }
        if (context.getUserType() != null) {
            RpcContext.getClientAttachment().setAttachment(HeaderConstant.USER_TYPE,
                    context.getUserType().toString());
        }
        if (context.getRoles() != null) {
            RpcContext.getClientAttachment().setAttachment(HeaderConstant.USER_ROLES,
                    context.getRoles());
        }
        if (context.getAuthorities() != null) {
            RpcContext.getClientAttachment().setAttachment(HeaderConstant.USER_AUTHORITIES,
                    context.getAuthorities());
        }
        if (context.getNickname() != null) {
            RpcContext.getClientAttachment().setAttachment(HeaderConstant.NICKNAME,
                    context.getNickname());
        }
        if (context.getPhone() != null) {
            RpcContext.getClientAttachment().setAttachment(HeaderConstant.PHONE,
                    context.getPhone());
        }
        if (context.getEmail() != null) {
            RpcContext.getClientAttachment().setAttachment(HeaderConstant.EMAIL,
                    context.getEmail());
        }
        if (context.getAttendingDoctorId() != null) {
            RpcContext.getClientAttachment().setAttachment(HeaderConstant.ATTENDING_DOCTOR_ID,
                    context.getAttendingDoctorId().toString());
        }
        if (context.getDepartmentId() != null) {
            RpcContext.getClientAttachment().setAttachment(HeaderConstant.DEPARTMENT_ID,
                    context.getDepartmentId().toString());
        }
    }
}