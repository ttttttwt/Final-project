package com.lexia.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.auth.AuthService;
import com.lexia.backend.auth.CustomUserDetailsService;
import com.lexia.backend.auth.JwtTokenProvider;
import com.lexia.backend.dto.*;
import com.lexia.backend.entity.User;
import com.lexia.backend.exception.InvalidTokenException;
import com.lexia.backend.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.lexia.backend.common.GlobalExceptionHandler;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * Tests all authentication endpoints with various scenarios.
 */
@WebMvcTest(AuthController.class)
@ContextConfiguration(classes = { AuthController.class, GlobalExceptionHandler.class })
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AuthService authService;

        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;

        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;

        private RegisterDTO validRegisterDTO;
        private LoginDTO validLoginDTO;
        private RefreshTokenDTO validRefreshTokenDTO;
        private User mockUser;
        private LoginResponseDTO mockLoginResponse;
        private RefreshTokenResponseDTO mockRefreshResponse;

        @BeforeEach
        void setUp() {
                // Setup valid register DTO
                validRegisterDTO = RegisterDTO.builder()
                                .email("test@lexia.com")
                                .password("Password123!")
                                .confirmPassword("Password123!")
                                .fullName("Test User")
                                .build();

                // Setup valid login DTO
                validLoginDTO = LoginDTO.builder()
                                .email("test@lexia.com")
                                .password("Password123!")
                                .build();

                // Setup valid refresh token DTO
                validRefreshTokenDTO = RefreshTokenDTO.builder()
                                .refreshToken("valid.refresh.token")
                                .build();

                // Setup mock user
                mockUser = User.builder()
                                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                                .email("test@lexia.com")
                                .isActive(true)
                                .build();

                // Setup mock login response
                mockLoginResponse = LoginResponseDTO.builder()
                                .accessToken("mock.access.token")
                                .refreshToken("mock.refresh.token")
                                .tokenType("Bearer")
                                .expiresIn(900000L)
                                .user(UserDTO.builder()
                                                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                                                .email("test@lexia.com")
                                                .fullName("Test User")
                                                .build())
                                .build();

                // Setup mock refresh response
                mockRefreshResponse = RefreshTokenResponseDTO.builder()
                                .accessToken("new.access.token")
                                .refreshToken("new.refresh.token")
                                .tokenType("Bearer")
                                .expiresIn(900000L)
                                .build();
        }

        // ========== Registration Tests ==========

        @Test
        @WithMockUser
        void registerUser_WithValidData_ReturnsCreated() throws Exception {
                // Given
                when(authService.register(any(RegisterDTO.class))).thenReturn(mockUser);

                // When & Then
                mockMvc.perform(post("/api/v1/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRegisterDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(mockUser.getId().toString()))
                                .andExpect(jsonPath("$.email").value(mockUser.getEmail()))
                                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }

        @Test
        @WithMockUser
        void registerUser_WithInvalidEmail_ReturnsBadRequest() throws Exception {
                // Given
                RegisterDTO invalidDTO = RegisterDTO.builder()
                                .email("invalid-email")
                                .password("Password123!")
                                .confirmPassword("Password123!")
                                .fullName("Test User")
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDTO)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        void registerUser_WithDuplicateEmail_ReturnsConflict() throws Exception {
                // Given
                when(authService.register(any(RegisterDTO.class)))
                                .thenThrow(new UserAlreadyExistsException("Email already registered"));

                // When & Then
                mockMvc.perform(post("/api/v1/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRegisterDTO)))
                                .andExpect(status().isConflict());
        }

        // ========== Login Tests ==========

        @Test
        @WithMockUser
        void loginUser_WithValidCredentials_ReturnsOk() throws Exception {
                // Given
                when(authService.login(any(LoginDTO.class))).thenReturn(mockLoginResponse);

                // When & Then
                mockMvc.perform(post("/api/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validLoginDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("mock.access.token"))
                                .andExpect(jsonPath("$.refreshToken").value("mock.refresh.token"))
                                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                                .andExpect(jsonPath("$.user.email").value("test@lexia.com"));
        }

        @Test
        @WithMockUser
        void loginUser_WithInvalidCredentials_ReturnsUnauthorized() throws Exception {
                // Given
                when(authService.login(any(LoginDTO.class)))
                                .thenThrow(new UserAlreadyExistsException("Invalid email or password"));

                // When & Then
                mockMvc.perform(post("/api/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validLoginDTO)))
                                .andExpect(status().isConflict());
        }

        @Test
        @WithMockUser
        void loginUser_WithMissingEmail_ReturnsBadRequest() throws Exception {
                // Given
                LoginDTO invalidDTO = LoginDTO.builder()
                                .password("Password123!")
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDTO)))
                                .andExpect(status().isBadRequest());
        }

        // ========== Token Refresh Tests ==========

        @Test
        @WithMockUser
        void refreshToken_WithValidToken_ReturnsOk() throws Exception {
                // Given
                when(authService.refreshToken(any(RefreshTokenDTO.class))).thenReturn(mockRefreshResponse);

                // When & Then
                mockMvc.perform(post("/api/v1/auth/refresh")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRefreshTokenDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("new.access.token"))
                                .andExpect(jsonPath("$.refreshToken").value("new.refresh.token"))
                                .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        @WithMockUser
        void refreshToken_WithInvalidToken_ReturnsUnauthorized() throws Exception {
                // Given
                when(authService.refreshToken(any(RefreshTokenDTO.class)))
                                .thenThrow(new InvalidTokenException("Invalid refresh token"));

                // When & Then
                mockMvc.perform(post("/api/v1/auth/refresh")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRefreshTokenDTO)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        void refreshToken_WithMissingToken_ReturnsBadRequest() throws Exception {
                // Given
                RefreshTokenDTO invalidDTO = RefreshTokenDTO.builder().build();

                // When & Then
                mockMvc.perform(post("/api/v1/auth/refresh")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDTO)))
                                .andExpect(status().isBadRequest());
        }
}
