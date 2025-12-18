package com.lexia.backend.config;

import com.lexia.backend.auth.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for LEXIA backend.
 * Configures authentication, authorization, and security filters.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    /**
     * Configure HTTP security for the application.
     * Allows public access to authentication endpoints while securing others.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS with custom configuration for frontend integration
                .cors(Customizer.withDefaults())

                // Disable CSRF for stateless API (using JWT)
                .csrf(csrf -> csrf.disable())

                // Disable anonymous authentication so unauthenticated access yields 401
                .anonymous(anonymous -> anonymous.disable())

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to authentication endpoints
                        .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh")
                        .permitAll()

                        // Allow public access to WebSocket endpoints (auth handled at STOMP level)
                        .requestMatchers("/ws/**", "/ws").permitAll()

                        // Allow public access to Swagger UI and OpenAPI documentation
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**")
                        .permitAll()

                        // Allow public access to H2 console (development only)
                        .requestMatchers("/h2-console/**").permitAll()

                        // Allow public access to actuator health endpoints for uptime checks
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

                        // Restrict remaining actuator endpoints to ADMINs only
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Allow public access to error endpoints
                        .requestMatchers("/error").permitAll()

                        // Allow public access to file downloads (access control handled in controller)
                        .requestMatchers("/api/v1/files/*/download").permitAll()

                        // Allow public access to Stripe Webhook
                        .requestMatchers("/api/v1/webhook/stripe").permitAll()

                        // All other requests require authentication
                        .anyRequest().authenticated())

                // Use stateless session management (for JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Customize unauthorized and forbidden responses for clarity
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint((request, response, authException) -> response
                                .sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                            if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                            } else {
                                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                            }
                        }))

                // Add JWT filter before the username/password authentication filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

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

    /**
     * Configure CORS to allow the frontend and mobile apps to communicate with the
     * API.
     * Supports credentials for authentication.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow all origins in development (for mobile emulators)
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration
                .setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
