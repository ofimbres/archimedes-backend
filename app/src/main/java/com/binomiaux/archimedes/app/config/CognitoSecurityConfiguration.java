package com.binomiaux.archimedes.app.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
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

    // prod
    //@Profile("!dev")
//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//                 .csrf().disable()
//                 .oauth2Client()
//                 .and()
//                 .logout()
//                 .and()
//                 .oauth2Login()
//                 .redirectionEndpoint().baseUri("/login/oauth2/code/cognito");


//         /*http.authorizeRequests()
// >>>>>>> Stashed changes
//                 .antMatchers("/healthcheck/").permitAll()
// =======
        
//         http.authorizeHttpRequests()
//                 .requestMatchers("/healthcheck/").permitAll()
// >>>>>>> Stashed changes
//                 .anyRequest().authenticated().and()
//                 .oauth2ResourceServer().jwt();

//         http.cors();*/

//         return http.csrf().disable().build();
//         //return http.build();
//     }

    //@Profile("dev")
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf ->
                    csrf.disable())
                .authorizeHttpRequests(requests -> 
                    requests.anyRequest().permitAll())
                .cors((cors -> {
                    cors.configurationSource(corsConfigurationSource());
                }));

        return http.build();
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            try {
                OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) new ArrayList<>(authorities).get(0);

                mappedAuthorities = ((ArrayList<?>) oidcUserAuthority.getAttributes().get("cognito:groups")).stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(Collectors.toSet());
            } catch (Exception exception) {
                System.out.println("Not Authorized!");

                System.out.println(exception.getMessage());
            }

            return mappedAuthorities;
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(cors.getAllowedOrigins());
        configuration.setAllowedMethods(cors.getAllowedMethods());
        configuration.setMaxAge(cors.getMaxAge());
        configuration.setAllowedHeaders(cors.getAllowedHeaders());
        configuration.setExposedHeaders(cors.getExposedHeaders());
        // configuration.addAllowedOrigin("http://localhost:3000"); // Allow all origins, change this to specific origins in production
        // configuration.addAllowedMethod("*"); // Allow all methods (GET, POST, etc.)
        // configuration.addAllowedHeader("*"); // Allow all headers
        // configuration.setAllowCredentials(true); // Allow credentials

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}