package com.normcat.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NormCat API — E-Commerce Catalog & Normalization Platform")
                        .version("1.0.0")
                        .description("Production-ready API for multi-vendor product catalog management, " +
                                "deterministic attribute normalization, and high-performance faceted search.")
                        .contact(new Contact()
                                .name("ThinhLS")
                                .url("https://github.com/D60-ThinhLS-ImprovementPlan/normcat-backend"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
