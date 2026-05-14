package com.greengrassland.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI greenGrasslandOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("青青草原 API")
                        .description("校园同好活动平台 - 找到附近兴趣相同的人")
                        .version("3.0.0")
                        .contact(new Contact().name("GreenGrassland").url("https://github.com/smallcups/GreenGrassland"))
                        .license(new License().name("MIT")));
    }
}
