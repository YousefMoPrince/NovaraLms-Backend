package com.amarjo.novaralms.course.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class securityCoursesConfig {
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf->csrf.disable()).authorizeHttpRequests(
                auth -> auth
                        .requestMatchers("/api/v1/course").permitAll()
                .requestMatchers("/api/v1/course/createCourse").hasRole("Instructor")
                .requestMatchers("/api/v1/course/{courseCode}/thumbnail").hasRole("Instructor")
                .requestMatchers("/api/v1/course/{courseCode}/complete").hasRole("Instructor")
                .requestMatchers("/api/v1/course/createCourse/{courseCode}/update").hasRole("Instructor")
                .requestMatchers("/api/v1/course/createCourse/{courseCode}/delete").hasRole("Instructor")
                .requestMatchers("/api/v1/course/createCourse/{courseCode}/getCourse").permitAll()
                .requestMatchers("/api/v1/course/createCourse/courses").permitAll()
                .requestMatchers("/api/v1/course/createCourse/instructorCourses/{instructorCode}").permitAll()
                .anyRequest().authenticated()
        ).build();
    }
}
