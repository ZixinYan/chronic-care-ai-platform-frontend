package com.zixin.healthcenterprovider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 健康报告中心 Provider 启动类
 * 
 * @author zixin
 */
@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.zixin.healthcenterprovider", "com.zixin.utils"})
public class HealthCenterProviderApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(HealthCenterProviderApplication.class, args);
    }
}
