package com.fc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@EnableConfigurationProperties
@EnableScheduling
@Slf4j
public class FilmApplication {
    public static void main(String[] args) {
        SpringApplication.run(FilmApplication.class, args);
        log.info("server started");

        // 打印可用的端点
        log.info("Swagger UI should be available at:");
        log.info("http://localhost:8080/swagger-ui.html");
        log.info("API docs at: http://localhost:8080/v3/api-docs");
    }
}
