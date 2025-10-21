package com.lexia.backend.common;

import com.lexia.backend.exception.InvalidTokenException;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.exception.UserAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests all exception handling scenarios.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/test");
    }

    // ========== Validation Exception Tests ==========

    @Test
    void handleValidationExceptions_WithFieldErrors_ReturnsBadRequest() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError emailError = new FieldError("registerDTO", "email", "must be a valid email");
        FieldError passwordError = new FieldError("registerDTO", "password", "must be at least 8 characters");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(emailError, passwordError));

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception, mockRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Input validation failed. Please check the validation errors.", response.getBody().getMessage());
        assertEquals("Validation Failed", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("/api/v1/test", response.getBody().getPath());
        
        List<ValidationError> validationErrors = response.getBody().getValidationErrors();
        assertNotNull(validationErrors);
        assertEquals(2, validationErrors.size());
        assertEquals("email", validationErrors.get(0).getField());
        assertEquals("must be a valid email", validationErrors.get(0).getMessage());
    }

    // ========== Authentication Exception Tests ==========

    @Test
    void handleAuthenticationException_WithBadCredentials_ReturnsUnauthorized() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(exception, mockRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid email or password", response.getBody().getMessage());
        assertEquals("Authentication Failed", response.getBody().getError());
        assertEquals(401, response.getBody().getStatus());
    }

    // ========== Token Exception Tests ==========

    @Test
    void handleInvalidTokenException_ReturnsUnauthorized() {
        // Given
        InvalidTokenException exception = new InvalidTokenException("Token expired");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidTokenException(exception, mockRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Token expired", response.getBody().getMessage());
        assertEquals("Invalid Token", response.getBody().getError());
        assertEquals(401, response.getBody().getStatus());
    }

    // ========== User Already Exists Exception Tests ==========

    @Test
    void handleUserAlreadyExistsException_ReturnsConflict() {
        // Given
        UserAlreadyExistsException exception = new UserAlreadyExistsException("Email already registered");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserAlreadyExistsException(exception, mockRequest);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Email already registered", response.getBody().getMessage());
        assertEquals("User Already Exists", response.getBody().getError());
        assertEquals(409, response.getBody().getStatus());
    }

    // ========== Resource Not Found Exception Tests ==========

    @Test
    void handleResourceNotFoundException_ReturnsNotFound() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("User not found");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(exception, mockRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().getMessage());
        assertEquals("Resource Not Found", response.getBody().getError());
        assertEquals(404, response.getBody().getStatus());
    }

    // ========== Access Denied Exception Tests ==========

    @Test
    void handleAccessDeniedException_ReturnsForbidden() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(exception, mockRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("You don't have permission to access this resource", response.getBody().getMessage());
        assertEquals("Access Denied", response.getBody().getError());
        assertEquals(403, response.getBody().getStatus());
    }

    // ========== Illegal Argument Exception Tests ==========

    @Test
    void handleIllegalArgumentException_ReturnsBadRequest() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception, mockRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid input", response.getBody().getMessage());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }

    // ========== Generic Exception Tests ==========

    @Test
    void handleGenericException_ReturnsInternalServerError() {
        // Given
        Exception exception = new RuntimeException("Unexpected error occurred");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, mockRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals(500, response.getBody().getStatus());
    }

    // ========== Error Response Structure Tests ==========

    @Test
    void errorResponse_ContainsRequiredFields() {
        // Given
        Exception exception = new RuntimeException("Test error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, mockRequest);

        // Then
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("/api/v1/test", response.getBody().getPath());
    }
}
