package com.zixin.doctorconsumer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 医生服务Consumer启动类
 * 
 * 提供C端(医生工作台)访问的HTTP接口
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@ComponentScan(basePackages = {"com.zixin.doctorconsumer","com.zixin.utils"})
@EnableDubbo
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class DoctorConsumerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DoctorConsumerApplication.class, args);
    }
}
