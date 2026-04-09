package com.amarjo.novaralms.course.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class enrollmentSecurityConfig {
    @Bean
    @Order(4)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf->csrf.disable()).authorizeHttpRequests(
                auth -> auth
                        .requestMatchers("/api/v1/enroll").permitAll()
                        .requestMatchers("/api/v1/enroll/enrollstudent").hasRole("student")
                        .requestMatchers("/api/v1/enroll/{courseCode}/enrollmentcount").hasRole("Instructor")
                        .anyRequest().authenticated()
        ).build();
    }
}
