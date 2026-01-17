package com.gpustore.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing user.
 *
 * <p>All fields are optional. Only non-null fields will be updated.</p>
 *
 * @param name     the user's new display name (optional, max 100 chars)
 * @param email    the user's new email address (optional, must be valid, max 100 chars)
 * @param password the user's new password (optional, min 6 chars)
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record UpdateUserRequest(
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must be at most 100 characters")
        String email,

        @Size(min = 6, message = "Password must be at least 6 characters")
        String password
) {
}
