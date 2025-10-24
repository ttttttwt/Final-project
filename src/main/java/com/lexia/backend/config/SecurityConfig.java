package com.lexia.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for LEXIA backend.
 * Configures authentication, authorization, and security filters.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure HTTP security for the application.
     * Allows public access to authentication endpoints while securing others.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless API (using JWT)
                .csrf(csrf -> csrf.disable())

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to authentication endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Allow public access to H2 console (development only)
                        .requestMatchers("/h2-console/**").permitAll()

                        // Allow public access to error endpoints
                        .requestMatchers("/error").permitAll()

                        // All other requests require authentication
                        .anyRequest().authenticated())

                // Use stateless session management (for JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Allow frames for H2 console (development only)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    /**
     * BCrypt password encoder bean.
     * Cost factor of 12 for secure password hashing.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
