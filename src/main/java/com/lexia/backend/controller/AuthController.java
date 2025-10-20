package com.lexia.backend.controller;

import com.lexia.backend.auth.AuthService;
import com.lexia.backend.dto.RegisterDTO;
import com.lexia.backend.dto.UserDTO;
import com.lexia.backend.entity.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller for LEXIA.
 * Handles user registration, login, and token management endpoints.
 *
 * Base path: /api/v1/auth
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user account.
     *
     * @param registerDTO the registration data
     * @return ResponseEntity with user data or error message
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterDTO registerDTO) {
        LOG.info("Received registration request for email: {}", registerDTO.getEmail());

        try {
            // Register the user using AuthService
            User registeredUser = authService.register(registerDTO);

            // Convert to DTO for response (excludes sensitive data)
            UserDTO userDTO = UserDTO.fromEntity(registeredUser);

            LOG.info("User registered successfully with ID: {}", registeredUser.getId());

            // Return 201 Created with user data
            return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);

        } catch (IllegalArgumentException e) {
            // Handle validation errors (email already exists, etc.)
            LOG.warn("Registration failed for email {}: {}", registerDTO.getEmail(), e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            // Handle unexpected errors
            LOG.error("Unexpected error during user registration for email: {}", registerDTO.getEmail(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", "An unexpected error occurred during registration");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}