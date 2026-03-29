package com.zixin.healthcenterconsumer.config;

import com.zixin.utils.interceptor.UserInfoExtractInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 
 * 注册用户信息提取拦截器
 * 
 * @author zixin
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private UserInfoExtractInterceptor userInfoExtractInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册用户信息提取拦截器
        registry.addInterceptor(userInfoExtractInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(    // 排除不需要用户信息的路径
                        "/error",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }
}
