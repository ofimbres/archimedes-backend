package com.binomiaux.archimedes.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@SpringBootApplication
@ComponentScan({ "com.binomiaux.archimedes.dao", "com.binomiaux.archimedes.service", "com.binomiaux.archimedes.business", "com.binomiaux.archimedes.database" })
@EnableAutoConfiguration(exclude={SecurityAutoConfiguration.class})
public class ArchimedesApplication {
	public static void main(String[] args) {
		SpringApplication.run(ArchimedesApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("http://localhost:3001");
			}
		};
	}
}
