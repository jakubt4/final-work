package com.gpustore.auth.dto;

/**
 * Response DTO containing authentication token information.
 *
 * @param token     the JWT token string
 * @param type      the token type (always "Bearer")
 * @param expiresIn token validity duration in milliseconds
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record LoginResponse(
        String token,
        String type,
        long expiresIn
) {
    /**
     * Factory method to create a LoginResponse with Bearer token type.
     *
     * @param token     the JWT token string
     * @param expiresIn token validity duration in milliseconds
     * @return a new LoginResponse instance
     */
    public static LoginResponse of(String token, long expiresIn) {
        return new LoginResponse(token, "Bearer", expiresIn);
    }
}
