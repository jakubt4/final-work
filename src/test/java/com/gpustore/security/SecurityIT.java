package com.gpustore.security;

import com.gpustore.AbstractIntegrationTest;
import com.gpustore.auth.dto.LoginRequest;
import com.gpustore.user.dto.CreateUserRequest;
import com.gpustore.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityIT extends AbstractIntegrationTest {

    @Test
    void protectedEndpoint_WithoutToken_ReturnsUnauthorized() {
        // When - access protected endpoint without token
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/users",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpoint_WithValidToken_ReturnsOk() {
        // Given - register and login
        String token = getAuthToken();

        // When - access protected endpoint with valid token
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
    void protectedEndpoint_WithMalformedToken_ReturnsUnauthorized() {
        // Given - malformed token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("not.a.valid.token.format");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // When - access protected endpoint with malformed token
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpoint_WithInvalidToken_ReturnsUnauthorized() {
        // Given - token with valid structure but invalid signature
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // When - access protected endpoint with invalid token
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void userRegistration_WithoutToken_AllowsAccess() {
        // Given - registration endpoint should be public
        CreateUserRequest request = new CreateUserRequest(
                "Public User",
                "public@example.com",
                "password123"
        );

        // When - register without any authentication
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                "/api/users",
                request,
                UserResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo("public@example.com");
    }

    @Test
    void loginEndpoint_WithoutToken_AllowsAccess() {
        // Given - create a user first
        CreateUserRequest userRequest = new CreateUserRequest(
                "Login Test User",
                "logintest@example.com",
                "password123"
        );
        restTemplate.postForEntity("/api/users", userRequest, UserResponse.class);

        // When - login without any authentication
        LoginRequest loginRequest = new LoginRequest("logintest@example.com", "password123");
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void productsEndpoint_WithoutToken_ReturnsUnauthorized() {
        // When - access products endpoint without token
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/products",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void ordersEndpoint_WithoutToken_ReturnsUnauthorized() {
        // When - access orders endpoint without token
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/orders",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
