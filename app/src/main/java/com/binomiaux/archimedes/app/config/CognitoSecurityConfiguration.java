package com.binomiaux.archimedes.app.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Congnito Security Configuration for:
 * a) Cognito interactive/web users authentication and
 * b) Cognito REST clients authentication - token based security.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({ Cors.class })
public class CognitoSecurityConfiguration {
    private final Cors cors;

    public CognitoSecurityConfiguration(Cors cors) {
        this.cors = cors;
    }

    @Profile("!dev")
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .oauth2Client()
                .and()
                .logout()
                .and()
                .oauth2Login()
                .redirectionEndpoint().baseUri("/login/oauth2/code/cognito");

        http.authorizeRequests()
                .antMatchers("/healthcheck/").permitAll()
                .anyRequest().authenticated().and()
                .oauth2ResourceServer().jwt();

        http.cors();

        return http.build();
    }

    @Profile("dev")
    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll();

        http.cors();

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(cors.getAllowedOrigins());
        configuration.setAllowedMethods(cors.getAllowedMethods());
        configuration.setMaxAge(cors.getMaxAge());
        configuration.setAllowedHeaders(cors.getAllowedHeaders());
        configuration.setExposedHeaders(cors.getExposedHeaders());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
