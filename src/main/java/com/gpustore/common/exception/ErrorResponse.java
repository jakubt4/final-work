package com.gpustore.common.exception;

import java.time.LocalDateTime;

/**
 * Standard error response record for API error handling.
 *
 * <p>Provides a consistent structure for all error responses returned by the API,
 * including HTTP status code, error message, and timestamp.</p>
 *
 * @param status    the HTTP status code of the error
 * @param message   a descriptive error message
 * @param timestamp the time when the error occurred
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record ErrorResponse(
    int status,
    String message,
    LocalDateTime timestamp
) {
    /**
     * Convenience constructor that automatically sets the timestamp to the current time.
     *
     * @param status  the HTTP status code of the error
     * @param message a descriptive error message
     */
    public ErrorResponse(int status, String message) {
        this(status, message, LocalDateTime.now());
    }
}
