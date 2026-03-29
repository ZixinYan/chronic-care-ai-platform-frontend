package com.zixin.messageconsumer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 站内信系统Consumer启动类
 * 
 * 负责提供C端(前端用户)访问的接口
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@ComponentScan(basePackages = {
        "com.zixin.messageconsumer",
        "com.zixin.utils"
})
@EnableDubbo
public class MessageConsumerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MessageConsumerApplication.class, args);
    }
}
