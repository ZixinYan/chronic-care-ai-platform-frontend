package com.zixin.messageprovider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 站内信系统Provider启动类
 */
@SpringBootApplication
@EnableDubbo
@MapperScan("com.zixin.messageprovider.mapper")
public class MessageProviderApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MessageProviderApplication.class, args);
    }
}
