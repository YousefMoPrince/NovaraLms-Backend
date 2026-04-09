package com.amarjo.novaralms.course.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class securityVideoConfig {
    @Bean
    @Order(3)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf->csrf.disable()).authorizeHttpRequests(
                auth -> auth
                        .requestMatchers("/api/v1/video").permitAll()
                .requestMatchers("/api/v1/video/addvideo").hasRole("Instructor")
                        .requestMatchers("/api/v1/video/{courseCode}/uploadvideothumbnail").hasRole("Instructor")
                        .requestMatchers("/api/v1/video//{courseCode}/uploadvideo").hasRole("Instructor")
                        .requestMatchers("/api/v1/video/{videoId}/finalizevideo").hasRole("Instructor")
                        .requestMatchers("/api/v1/video/{videoId}/deletevideo").hasRole("Instructor")
                        .requestMatchers("/api/v1/video/{videoId}/getvideo").permitAll()
                        .requestMatchers("/api/v1/video/{videoId}/allvideos").permitAll()
                        .anyRequest().authenticated()

        ).build();
    }
}
