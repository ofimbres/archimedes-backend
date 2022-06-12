package com.binomiaux.archimedes.service.config;

import com.binomiaux.archimedes.business.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//@Configuration
//@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    //@Autowired
    JwtTokenProvider jwtTokenProvider;

    //@Bean
    /*public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }*/

    //@Bean
    /*public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }*/

    //@Bean
    /*public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }*/

    //@Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            /*.httpBasic().disable().csrf().disable().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().*/
            .authorizeRequests()
                .antMatchers("/user/login", "activity/").anonymous()
                .antMatchers(HttpMethod.POST,  "activity/create").permitAll()
                .and().csrf().disable();
                //antMatchers("/user/potro")
                //    .permitAll().anyRequest().authenticated()
                //.antMatchers("/user/login")
                //    .()
                    //.permitAll()
                //.antMatchers("/**")
                //    .hasAnyRole("ADMIN", "USER")
                //.and()
                //    .apply(new JwtConfigurer(jwtTokenProvider));

    }
}
