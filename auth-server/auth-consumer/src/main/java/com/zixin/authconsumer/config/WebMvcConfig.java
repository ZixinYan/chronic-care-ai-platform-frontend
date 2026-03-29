package com.zixin.authconsumer.config;

import com.zixin.utils.interceptor.UserInfoExtractInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 注册拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserInfoExtractInterceptor userInfoInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册用户信息拦截器
        registry.addInterceptor(userInfoInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        "/auth/login",     // 登录接口不需要用户信息
                        "/auth/register",  // 注册接口不需要用户信息
                        "/auth/sms/code",  // 短信验证码不需要用户信息
                        "/error"          // 错误页面
                );
    }

    WebMvcConfig(UserInfoExtractInterceptor userInfoInterceptor) {
        this.userInfoInterceptor = userInfoInterceptor;
    }
}
