package com.gpustore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user authentication.
 *
 * @param email    the user's email address (required, must be valid)
 * @param password the user's password (required)
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
}
