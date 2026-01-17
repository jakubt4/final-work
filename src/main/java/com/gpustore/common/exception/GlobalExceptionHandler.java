package com.gpustore.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for the GPU E-commerce Platform API.
 *
 * <p>Provides centralized exception handling across all controllers, converting
 * exceptions into standardized {@link ErrorResponse} objects with appropriate
 * HTTP status codes.</p>
 *
 * <p>Handles the following exception types:</p>
 * <ul>
 *   <li>{@link ResourceNotFoundException} - Returns 404 Not Found</li>
 *   <li>{@link ValidationException} - Returns 400 Bad Request</li>
 *   <li>{@link MethodArgumentNotValidException} - Returns 400 Bad Request with validation errors</li>
 *   <li>{@link BadCredentialsException} - Returns 401 Unauthorized</li>
 *   <li>{@link Exception} - Returns 500 Internal Server Error (fallback)</li>
 * </ul>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles resource not found exceptions.
     *
     * @param ex the exception thrown when a requested resource is not found
     * @return a 404 response with error details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles custom validation exceptions.
     *
     * @param ex the validation exception with details about the validation failure
     * @return a 400 response with validation error details
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handles bean validation exceptions from {@code @Valid} annotations.
     *
     * <p>Collects all field validation errors and returns them as a comma-separated message.</p>
     *
     * @param ex the exception containing binding/validation errors
     * @return a 400 response with all validation error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

        log.warn("Method argument validation failed: {}", message);
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            message
        );
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handles authentication failures due to bad credentials.
     *
     * @param ex the exception thrown when authentication credentials are invalid
     * @return a 401 response with a generic authentication error message
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: bad credentials");
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Invalid email or password"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Fallback handler for all uncaught exceptions.
     *
     * <p>Returns a generic error message to avoid exposing internal details.</p>
     *
     * @param ex the unhandled exception
     * @return a 500 response with a generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal server error"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
