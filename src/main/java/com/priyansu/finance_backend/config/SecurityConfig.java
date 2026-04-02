package com.priyansu.finance_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity  // for @PreAuthorize
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // temporary (will secure later)
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> org.springframework.security.core.userdetails.User
                .withUsername("admin")
                .password("{noop}password")
                .roles("ADMIN") // ROLE_ADMIN internally (means:Spring automatically prefixes ROLE_)...
                .build();
    }
}