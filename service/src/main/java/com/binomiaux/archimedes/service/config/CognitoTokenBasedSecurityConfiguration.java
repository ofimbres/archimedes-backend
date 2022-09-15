package com.binomiaux.archimedes.service.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

@Slf4j
@Configuration
@Order(1)
public class CognitoTokenBasedSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @SneakyThrows
    @Override
    protected void configure(HttpSecurity http) {
        http
                .requestMatcher(new RequestHeaderRequestMatcher("Authorization"))
                .authorizeRequests().anyRequest().authenticated()
                .and().oauth2ResourceServer().jwt();
    }
}
