package com.zixin.healthcenterconsumer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 健康报告中心 Consumer 启动类
 * 
 * @author zixin
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@EnableDubbo
@ComponentScan(basePackages = {"com.zixin.healthcenterconsumer", "com.zixin.utils"})
public class HealthCenterConsumerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(HealthCenterConsumerApplication.class, args);
    }
}
