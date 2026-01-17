package com.gpustore.common.exception;

/**
 * Exception thrown when business validation rules are violated.
 *
 * <p>This exception is handled by {@link GlobalExceptionHandler} and results
 * in a 400 Bad Request HTTP response.</p>
 *
 * <p>Use this exception for custom validation logic beyond Jakarta Bean Validation,
 * such as checking business rules like duplicate email addresses or insufficient stock.</p>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class ValidationException extends RuntimeException {

    /**
     * Constructs a new ValidationException with the specified message.
     *
     * @param message the detail message describing the validation failure
     */
    public ValidationException(String message) {
        super(message);
    }
}
