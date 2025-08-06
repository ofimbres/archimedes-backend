package com.binomiaux.archimedes.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Archimedes Backend API")
                        .description("Backend API for the Archimedes educational platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Binomiaux Team")
                                .email("support@binomiaux.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server"),
                        new Server().url("https://api.archimedes.edu").description("Production Server")
                ));
    }
}
