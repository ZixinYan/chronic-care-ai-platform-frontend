package com.zixin.authconsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@ComponentScan(basePackages = {
        "com.zixin",  // 扫描整个 com.zixin 包
        "com.zixin.authconsumer",
        "com.zixin.authconsumer.controller",
        "com.zixin.authconsumer.service",
        "com.zixin.authconsumer.config",
        "com.zixin.authconsumer.client",
        "com.zixin.utils"
})
@SpringBootApplication
public class AuthConsumerApplication implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(AuthConsumerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========== 所有注册的 Controller ==========");

        // 获取所有带有 @RestController 注解的 bean
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
        for (Map.Entry<String, Object> entry : controllers.entrySet()) {
            System.out.println("Bean name: " + entry.getKey() +
                    " -> Class: " + entry.getValue().getClass().getSimpleName());
        }
        System.out.println("==========================================\n");
    }

}
