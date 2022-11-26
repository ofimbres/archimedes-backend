package com.binomiaux.archimedes.app.config;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

/**
 *
 */
@Configuration
@EnableWebSecurity
@Order(1)
public class CognitoTokenBasedSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @SneakyThrows
    @Override
    protected void configure(HttpSecurity http) {
        http.requestMatcher(new RequestHeaderRequestMatcher("Authorization"))
            .authorizeRequests().anyRequest().authenticated()
            .and().oauth2ResourceServer().jwt();
    }
}
