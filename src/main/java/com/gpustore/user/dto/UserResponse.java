package com.gpustore.user.dto;

import com.gpustore.user.User;

import java.time.LocalDateTime;

/**
 * Response DTO representing user data returned by the API.
 *
 * <p>Excludes sensitive information like passwords.</p>
 *
 * @param id        the user's unique identifier
 * @param name      the user's display name
 * @param email     the user's email address
 * @param createdAt when the user was created
 * @param updatedAt when the user was last updated
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Creates a UserResponse from a User entity.
     *
     * @param user the user entity to convert
     * @return the response DTO
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
