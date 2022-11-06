package com.binomiaux.archimedes.app.config;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        // Needed for method access control via the @Secured annotation
        prePostEnabled = true,
        jsr250Enabled = true,
        securedEnabled = true
)
@Order(2)
public class CognitoSecurityConfiguration extends WebSecurityConfigurerAdapter {
    @SneakyThrows
    @Override
    protected void configure(HttpSecurity http) {
        /*http.csrf()
                .and()
                .authorizeRequests(authz -> authz.mvcMatchers("/")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2Login()
                .and()
                .logout()
                .logoutSuccessUrl("/");*/

        /*http
                // TODO disable CSRF because when enabled controllers aren't initialized
                //  and if they are, POST are getting 403
                .csrf().disable()

                .authorizeRequests()
                .anyRequest().authenticated()

                .and()
                .oauth2Client()

                .and()
                .logout()

                .and()
                .oauth2Login()
                .redirectionEndpoint().baseUri("/login/oauth2/code/cognito")
                .and();
                */

                http
                // TODO disable CSRF because when enabled controllers aren't initialized
                //  and if they are, POST are getting 403
                .csrf().disable()

                .authorizeRequests()
                .antMatchers("/**").permitAll()
                .anyRequest().authenticated()

                .and()
                .oauth2Client()

                .and()
                .logout()

                .and();

        http.cors();
    }
}
