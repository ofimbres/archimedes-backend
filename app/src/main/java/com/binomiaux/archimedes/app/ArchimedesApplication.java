package com.binomiaux.archimedes.app;

import com.binomiaux.archimedes.app.config.WebConfigProperties;
import com.binomiaux.archimedes.repository.ArchimedesRepositoryScan;
import com.binomiaux.archimedes.service.ArchimedesServiceScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The entry application.
 */
@Configuration
@SpringBootApplication
@Import(value = { ArchimedesServiceScan.class, ArchimedesRepositoryScan.class })
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
@EnableConfigurationProperties({WebConfigProperties.class})
public class ArchimedesApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArchimedesApplication.class, args);
    }

    private final WebConfigProperties webConfigProperties;

    public ArchimedesApplication(WebConfigProperties webConfigProperties) {
        this.webConfigProperties = webConfigProperties;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                WebConfigProperties.Cors cors = webConfigProperties.getCors();
                    registry.addMapping("/**")
                        .allowedOrigins(cors.getAllowedOrigins())
                        .allowedMethods(cors.getAllowedMethods())
                        .maxAge(cors.getMaxAge())
                        .allowedHeaders(cors.getAllowedHeaders())
                        .exposedHeaders(cors.getExposedHeaders());
            }
        };
    }
}
