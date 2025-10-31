package com.lexia.backend.common;

import com.lexia.backend.exception.CourseNotFoundException;
import com.lexia.backend.exception.DuplicateCourseException;
import com.lexia.backend.exception.InvalidInputException;
import com.lexia.backend.exception.InvalidLessonContentException;
import com.lexia.backend.exception.InvalidTokenException;
import com.lexia.backend.exception.LessonNotFoundException;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.exception.SectionNotFoundException;
import com.lexia.backend.exception.UserAlreadyExistsException;
import com.lexia.backend.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Global exception handler for the LEXIA backend.
 * Provides centralized error handling and consistent error responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        /**
         * Handle validation errors from @Valid annotations.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                LOG.warn("Validation error: {}", ex.getMessage());

                List<ValidationError> validationErrors = new ArrayList<>();

                // Extract field errors
                for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                        validationErrors.add(ValidationError.builder()
                                        .field(error.getField())
                                        .rejectedValue(error.getRejectedValue())
                                        .message(error.getDefaultMessage())
                                        .build());
                }

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Validation Failed")
                                .message("Input validation failed. Please check the validation errors.")
                                .path(request.getRequestURI())
                                .validationErrors(validationErrors)
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        /**
         * Handle constraint violation exceptions.
         */
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolationException(
                        ConstraintViolationException ex, HttpServletRequest request) {

                LOG.warn("Constraint violation: {}", ex.getMessage());

                List<ValidationError> validationErrors = new ArrayList<>();

                // Extract constraint violations
                Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
                for (ConstraintViolation<?> violation : violations) {
                        String fieldName = violation.getPropertyPath().toString();
                        // Extract field name from property path (e.g., "registerUser.registerDTO.email"
                        // -> "email")
                        if (fieldName.contains(".")) {
                                fieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
                        }

                        validationErrors.add(ValidationError.builder()
                                        .field(fieldName)
                                        .rejectedValue(violation.getInvalidValue())
                                        .message(violation.getMessage())
                                        .build());
                }

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Validation Failed")
                                .message("Input validation failed. Please check the validation errors.")
                                .path(request.getRequestURI())
                                .validationErrors(validationErrors)
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        /**
         * Handle user already exists exception.
         */
        @ExceptionHandler(UserAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
                        UserAlreadyExistsException ex, HttpServletRequest request) {

                LOG.warn("User already exists: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.CONFLICT.value())
                                .error("User Already Exists")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        /**
         * Handle resource not found exception.
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
                        ResourceNotFoundException ex, HttpServletRequest request) {

                LOG.warn("Resource not found: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .error("Resource Not Found")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        /**
         * Handle user not found exception.
         */
        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUserNotFoundException(
                        UserNotFoundException ex, HttpServletRequest request) {

                LOG.warn("User not found: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .error("User Not Found")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        /**
         * Handle invalid input exception.
         */
        @ExceptionHandler(InvalidInputException.class)
        public ResponseEntity<ErrorResponse> handleInvalidInputException(
                        InvalidInputException ex, HttpServletRequest request) {

                LOG.warn("Invalid input: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Invalid Input")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        /**
         * Handle invalid lesson content exception.
         * Returns 400 Bad Request with details about which validation rule failed.
         */
        @ExceptionHandler(InvalidLessonContentException.class)
        public ResponseEntity<ErrorResponse> handleInvalidLessonContentException(
                        InvalidLessonContentException ex, HttpServletRequest request) {

                LOG.warn("Invalid lesson content: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Invalid Lesson Content")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        /**
         * Handle course not found exception.
         * Returns 404 Not Found when a course doesn't exist.
         */
        @ExceptionHandler(CourseNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleCourseNotFoundException(
                        CourseNotFoundException ex, HttpServletRequest request) {

                LOG.warn("Course not found: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .error("Course Not Found")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        /**
         * Handle duplicate course exception.
         * Returns 409 Conflict when attempting to create a course with a duplicate
         * title.
         */
        @ExceptionHandler(DuplicateCourseException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateCourseException(
                        DuplicateCourseException ex, HttpServletRequest request) {

                LOG.warn("Duplicate course: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.CONFLICT.value())
                                .error("Duplicate Course")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        /**
         * Handle lesson not found exception.
         * Returns 404 Not Found when a lesson doesn't exist.
         */
        @ExceptionHandler(LessonNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleLessonNotFoundException(
                        LessonNotFoundException ex, HttpServletRequest request) {

                LOG.warn("Lesson not found: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .error("Lesson Not Found")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        /**
         * Handle section not found exception.
         * Returns 404 Not Found when a section doesn't exist.
         */
        @ExceptionHandler(SectionNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleSectionNotFoundException(
                        SectionNotFoundException ex, HttpServletRequest request) {

                LOG.warn("Section not found: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .error("Section Not Found")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        /**
         * Handle invalid token exceptions.
         */
        @ExceptionHandler(InvalidTokenException.class)
        public ResponseEntity<ErrorResponse> handleInvalidTokenException(
                        InvalidTokenException ex, HttpServletRequest request) {

                LOG.warn("Invalid token: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error("Invalid Token")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        /**
         * Handle authentication exceptions.
         */
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthenticationException(
                        AuthenticationException ex, HttpServletRequest request) {

                LOG.warn("Authentication failed: {}", ex.getMessage());

                String message = "Authentication failed";
                if (ex instanceof BadCredentialsException) {
                        message = "Invalid email or password";
                }

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error("Authentication Failed")
                                .message(message)
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        /**
         * Handle access denied exceptions.
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDeniedException(
                        AccessDeniedException ex, HttpServletRequest request) {

                LOG.warn("Access denied: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("Access Denied")
                                .message("You don't have permission to access this resource")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        /**
         * Handle illegal argument exceptions (business logic errors).
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
                        IllegalArgumentException ex, HttpServletRequest request) {

                LOG.warn("Illegal argument: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Bad Request")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        /**
         * Handle illegal state exceptions (business rule violations).
         * Returns 400 Bad Request when business rules prevent an operation.
         */
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ErrorResponse> handleIllegalStateException(
                        IllegalStateException ex, HttpServletRequest request) {

                LOG.warn("Illegal state: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Bad Request")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        /**
         * Handle all other exceptions.
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex, HttpServletRequest request) {

                LOG.error("Unexpected error occurred", ex);

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .error("Internal Server Error")
                                .message("An unexpected error occurred. Please try again later.")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
}