package com.gpustore.auth.dto;

public record LoginResponse(
        String token,
        String type,
        long expiresIn
) {
    public static LoginResponse of(String token, long expiresIn) {
        return new LoginResponse(token, "Bearer", expiresIn);
    }
}
