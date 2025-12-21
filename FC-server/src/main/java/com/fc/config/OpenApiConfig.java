package com.fc.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .openapi("3.1.0") // 明确设置 OpenAPI 版本
                .info(new Info()
                        .title("电影评论系统 API")
                        .version("1.0.0")
                        .description("电影评论系统接口文档")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("contact@example.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("本地开发服务器")
                ))
                .components(new Components()
                        .addSchemas("MultipartFile", new Schema()
                                .type("string")
                                .format("binary")
                                .description("文件")));
    }
}