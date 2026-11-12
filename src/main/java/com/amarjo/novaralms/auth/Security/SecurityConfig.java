package com.amarjo.novaralms.auth.Security;

import com.amarjo.novaralms.auth.Filter.JwtFilter;
import com.amarjo.novaralms.auth.Filter.PayOnWebhookAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    @Order(1)
    public SecurityFilterChain webhookFilterChain(HttpSecurity http, PayOnWebhookAuthFilter filter) throws Exception {
        return http
                .securityMatcher("/api/web/v1/payment/webhook/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session-> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/web/v1/payment/webhook/receive").permitAll()
                        .requestMatchers("/api/web/v1/payment/webhook/receipt/ack").authenticated()
                        .anyRequest().authenticated()
                ).addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    @Bean
    @Order(2)
    public SecurityFilterChain mobileFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        return http
                .securityMatcher("/api/mob/**", "/ws/mob/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/mob/v1/auth/login",
                                "/api/mob/v1/auth/register",
                                "/api/mob/v1/auth/refresh",
                                "/api/mob/v1/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/mob/v1/payment/verify/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/mob/v1/course/*/getCourse", "/api/mob/v1/course/instructorCourses/**","/api/mob/v1/course/courses/**","/api/mob/v1/video/*/all").permitAll()
                        .requestMatchers("/api/mob/v1/auth/me",
                                "/api/mob/v1/auth/uploadThumbnail",
                                "/api/mob/v1/auth/update","/api/mob/v1/status/change").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/mob/v1/course/createCourse").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/mob/v1/course/*/thumbnail").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/mob/v1/course/*/publish").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/mob/v1/course/*/update").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/mob/v1/course/*/archive").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/mob/v1/course/*/delete").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/mob/v1/video/add").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/mob/v1/video/*/upload/thumbnail/*").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/mob/v1/video/*/upload/video/*").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/mob/v1/video/*/finalize").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/mob/v1/video/*/delete").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/mob/v1/enroll/*/enrollment/count").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/mob/v1/enroll/pending").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.POST, "/api/mob/v1/enroll/*/ack").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/mob/v1/video/*/get","/api/web/v1/video/stream/**").authenticated()
                        .requestMatchers(HttpMethod.POST,"/api/mob/v1/payment/receipt/{courseCode}/create").authenticated()
                        .requestMatchers(HttpMethod.POST,"/api/mob/v1/chat/rooms/request","/api/mob/v1/chat/messages/send").authenticated()
                        .requestMatchers(HttpMethod.PATCH,"/api/mob/v1/chat/rooms/respond").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.GET,"/api/mob/v1/chat/rooms","/api/mob/v1/chat/rooms/*/messages").authenticated()
                        .requestMatchers("/api/mob/v1/notification/device/token").authenticated()
                        .requestMatchers(HttpMethod.POST,"/api/mob/v1/notification/send/topic").hasAnyRole("INSTRUCTOR","ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/mob/v1/notification","/api/mob/v1/notification/unread/count").authenticated()
                        .requestMatchers(HttpMethod.PATCH,"/api/mob/v1/notification/*/read","/api/mob/v1/notification/read/all").authenticated()
                        .requestMatchers("/ws/mob/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/web/**", "/ws/web/**")
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/api/web/v1/auth/login","/api/web/v1/auth/register","/api/web/v1/auth/logout","/api/web/v1/status/change","/ws/web/**")
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/web/v1/auth/login", "/api/web/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.PATCH,"/api/web/v1/status/change").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/web/v1/payment/verify/**").authenticated()
                        .requestMatchers("/api/web/v1/auth/me", "/api/web/v1/auth/update", "/api/web/v1/auth/uploadThumbnail","/api/web/v1/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/web/v1/course", "/api/web/v1/enroll",  "/api/web/v1/video/*/all").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/web/v1/course/*/getCourse", "/api/web/v1/course/instructorCourses/**","/api/web/v1/course/coursesStatus/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/web/v1/course/createCourse").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/web/v1/course/*/thumbnail").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/web/v1/course/*/publish").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/web/v1/course/*/update").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/web/v1/course/*/archive").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/web/v1/course/*/delete").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/web/v1/video/add").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/web/v1/video/*/upload/thumbnail/*").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/web/v1/video/*/upload/video/*").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/web/v1/video/*/finalize").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/web/v1/video/*/delete").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/web/v1/enroll/*/enrollment/count").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/web/v1/enroll/pending").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.POST, "/api/web/v1/enroll/ack").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/web/v1/video/*/get","/api/web/v1/video/stream/**").authenticated()
                        .requestMatchers(HttpMethod.POST,"/api/web/v1/payment/receipt/{courseCode}/create").authenticated()
                        .requestMatchers(HttpMethod.POST,"/api/web/v1/chat/rooms/request","/api/web/v1/chat/messages/send").authenticated()
                        .requestMatchers(HttpMethod.PATCH,"/api/web/v1/chat/rooms/respond").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.GET,"/api/web/v1/chat/rooms","/api/web/v1/chat/rooms/*/messages").authenticated()
                        .requestMatchers("/api/web/v1/notification/device/token").authenticated()
                        .requestMatchers(HttpMethod.POST,"/api/web/v1/notification/send/topic").hasAnyRole("INSTRUCTOR","ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/web/v1/notification","/api/web/v1/notification/unread/count").authenticated()
                        .requestMatchers(HttpMethod.PATCH,"/api/web/v1/notification/*/read","/api/web/v1/notification/read/all").authenticated()
                        .requestMatchers("/ws/web/**").authenticated()
                        .anyRequest().authenticated()
                ).build();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/v1/admin/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/admin/dashboard").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}