package org.example.rentingmanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger API文档配置
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("小区房屋租赁管理平台API")
                        .description("基于微信小程序的小区房屋租赁管理平台后端API文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@houserent.com")
                                .url("https://www.houserent.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
