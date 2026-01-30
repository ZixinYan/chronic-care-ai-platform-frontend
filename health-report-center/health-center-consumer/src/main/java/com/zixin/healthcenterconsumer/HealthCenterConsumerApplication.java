package com.zixin.healthcenterconsumer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 健康报告中心 Consumer 启动类
 * 
 * @author zixin
 */
@SpringBootApplication(scanBasePackages = {"com.zixin.healthcenterconsumer", "com.zixin.utils"})
@EnableDubbo
public class HealthCenterConsumerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(HealthCenterConsumerApplication.class, args);
    }
}
