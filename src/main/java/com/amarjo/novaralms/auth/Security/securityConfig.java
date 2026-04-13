package com.amarjo.novaralms.auth.Security;

import com.amarjo.novaralms.auth.Filter.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class securityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/v1/course","/api/v1/enroll","/api/v1/video/*/getvideo","/api/v1/video/*/allvideos").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/v1/course/courses", "/api/v1/course/*/getCourse", "/api/v1/course/instructorCourses/**").permitAll()



                        .requestMatchers(HttpMethod.POST,"/api/v1/course/createCourse").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST,"/api/v1/course/*/thumbnail").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST,"/api/v1/enroll/enrollstudent").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.POST,"/api/v1/video/addvideo").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST,"/api/v1/video/*/uploadvideothumbnail/*").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST,"/api/v1/video/*/uploadvideo/*").hasRole("INSTRUCTOR")

                        .requestMatchers(HttpMethod.PATCH,"/api/v1/video/*/finalizevideo").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT,"/api/v1/course/*/update").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PATCH,"/api/v1/course/*/complete").hasRole("INSTRUCTOR")

                        .requestMatchers(HttpMethod.DELETE,"/api/v1/video/*/deletevideo").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE,"/api/v1/course/*/delete").hasRole("INSTRUCTOR")

                        .requestMatchers(HttpMethod.GET,"/api/v1/enroll/*/enrollmentcount").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.GET,"/api/v1/video/stream/**").authenticated()



                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Configuration
    @EnableAspectJAutoProxy
    public class AopConfig {
    }
}