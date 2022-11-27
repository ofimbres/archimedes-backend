package com.binomiaux.archimedes.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 *
 */
@Configuration
@EnableWebSecurity
public class CognitoSecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Cognito interactive/web users authentication
        http
                .csrf().disable()
                .oauth2Client()
                .and()
                .logout()
                .and()
                .oauth2Login()
                .redirectionEndpoint().baseUri("/login/oauth2/code/cognito");

        // Cognito REST clients authentication - token based security
        http.authorizeRequests()
                .antMatchers("/healthcheck/").permitAll()
                .anyRequest().authenticated().and()
                .oauth2ResourceServer().jwt();

        return http.build();
    }
}
