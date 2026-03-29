package com.zixin.doctorprovider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 医生服务Provider启动类
 */
@SpringBootApplication
@EnableDubbo
@MapperScan("com.zixin.doctorprovider.mapper")
@ComponentScan({
        "com.zixin.doctorprovider",
        "com.zixin.utils.*"
}
)
public class DoctorProviderApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DoctorProviderApplication.class, args);
    }
}
