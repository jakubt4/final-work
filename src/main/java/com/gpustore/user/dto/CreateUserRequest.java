package com.gpustore.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new user.
 *
 * @param name     the user's display name (required, max 100 chars)
 * @param email    the user's email address (required, must be valid, max 100 chars)
 * @param password the user's password (required, min 6 chars)
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record CreateUserRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must be at most 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password
) {
}
