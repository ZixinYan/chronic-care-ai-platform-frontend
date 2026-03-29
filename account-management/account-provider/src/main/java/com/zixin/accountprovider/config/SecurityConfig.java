package com.zixin.accountprovider.config;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        // 强度10是默认值，可以根据需要调整（越高越安全但越慢）
        return new BCryptPasswordEncoder(5);
    }

    @Bean
    public DigestUtils digestUtils() {
        return new DigestUtils();
    }
}