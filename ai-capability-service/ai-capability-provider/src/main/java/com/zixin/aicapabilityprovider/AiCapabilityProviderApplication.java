package com.zixin.aicapabilityprovider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@EnableDubbo
@SpringBootApplication
@MapperScan("com.zixin.aicapabilityprovider.mapper")
@ComponentScan({"com.zixin.aicapabilityprovider", "com.zixin.utils.*"})
public class AiCapabilityProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCapabilityProviderApplication.class, args);
    }

}
