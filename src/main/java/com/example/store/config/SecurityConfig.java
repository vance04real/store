package com.example.store.config;

import com.example.store.payload.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Store API - This demo is just for Interview Implements Basic Authentication with
 * in-memory users. Production note: Replace with database/LDAP/OAuth2 authentication.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring HTTP Basic Authentication");

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/health")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(customAuthenticationEntryPoint()))
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        log.info("=== DEMO AUTHENTICATION SETUP ===");
        log.info("Creating in-memory users for interview demonstration");

        UserDetails user = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("store-admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN", "USER")
                .build();

        log.info("Demo credentials created:");
        log.info("Username: admin, Password: password (USER role)");
        log.info("Username: store-admin, Password: admin123 (ADMIN role)");
        log.warn("PRODUCTION: Replace with database/LDAP/OAuth2 authentication");

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            log.warn(
                    "Authentication failed for {} {}: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    authException.getMessage());

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ErrorResponse errorResponse = ErrorResponse.of(
                    "Full authentication is required to access this resource",
                    request.getRequestURI(),
                    HttpStatus.UNAUTHORIZED.value());

            try {
                String jsonResponse = objectMapper.writeValueAsString(errorResponse);
                response.getWriter().write(jsonResponse);
            } catch (Exception e) {
                log.error("Failed to serialize error response", e);

                String fallbackJson = String.format(
                        "{\"message\":\"%s\",\"timestamp\":\"%s\",\"path\":\"%s\",\"status\":%d}",
                        "Authentication required",
                        java.time.Instant.now().toString(),
                        request.getRequestURI(),
                        HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write(fallbackJson);
            }
        };
    }
}
