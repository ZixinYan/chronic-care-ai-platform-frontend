package com.zixin.authconsumer.interceptor;

import org.springframework.stereotype.Component;

/**
 * @deprecated 此类已迁移到utils模块
 * 请使用 {@link com.zixin.utils.security.UserInfoInterceptor} 替代
 */
@Deprecated
@Component
public class UserInfoInterceptor extends com.zixin.utils.security.UserInfoInterceptor {
    // 继承utils中的实现，保持向后兼容
}
