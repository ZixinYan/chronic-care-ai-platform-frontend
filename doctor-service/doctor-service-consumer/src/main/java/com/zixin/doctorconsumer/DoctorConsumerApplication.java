package com.zixin.doctorconsumer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 医生服务Consumer启动类
 * 
 * 提供C端(医生工作台)访问的HTTP接口
 */
@SpringBootApplication
@EnableDubbo
public class DoctorConsumerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DoctorConsumerApplication.class, args);
    }
}
