package com.zixin.aicapabilityconsumer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@ComponentScan({"com.zixin.aicapabilityconsumer", "com.zixin.utils.*"})
public class AiCapabilityConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCapabilityConsumerApplication.class, args);
    }

}
