package com.gpustore.auth;

import com.gpustore.AbstractIntegrationTest;
import com.gpustore.auth.dto.LoginRequest;
import com.gpustore.auth.dto.LoginResponse;
import com.gpustore.user.dto.CreateUserRequest;
import com.gpustore.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerIT extends AbstractIntegrationTest {

    private static final String TEST_EMAIL = "auth@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NAME = "Auth Test User";

    @BeforeEach
    void setUpUser() {
        // Register a test user
        CreateUserRequest request = new CreateUserRequest(TEST_NAME, TEST_EMAIL, TEST_PASSWORD);
        restTemplate.postForEntity("/api/users", request, UserResponse.class);
    }

    @Test
    void login_WithValidCredentials_ReturnsJwtToken() {
        // Given
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        // When
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                request,
                LoginResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotNull();
        assertThat(response.getBody().token()).isNotEmpty();
        assertThat(response.getBody().type()).isEqualTo("Bearer");
        assertThat(response.getBody().expiresIn()).isGreaterThan(0);
    }

    @Test
    void login_WithValidToken_CanAccessProtectedEndpoint() {
        // Given - login and get token
        LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                LoginResponse.class
        );
        String token = loginResponse.getBody().token();

        // When - use token to access protected endpoint
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void login_WithInvalidPassword_ReturnsUnauthorized() {
        // Given
        LoginRequest request = new LoginRequest(TEST_EMAIL, "wrongpassword");

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login",
                request,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_WithNonExistentEmail_ReturnsUnauthorized() {
        // Given
        LoginRequest request = new LoginRequest("nonexistent@example.com", TEST_PASSWORD);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login",
                request,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
