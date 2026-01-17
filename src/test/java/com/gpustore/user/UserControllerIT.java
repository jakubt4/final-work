package com.gpustore.user;

import com.gpustore.AbstractIntegrationTest;
import com.gpustore.user.dto.CreateUserRequest;
import com.gpustore.user.dto.UpdateUserRequest;
import com.gpustore.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerIT extends AbstractIntegrationTest {

    @Test
    void createUser_WithValidData_ReturnsCreatedUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest(
                "John Doe",
                "john@example.com",
                "password123"
        );

        // When
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                "/api/users",
                request,
                UserResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("John Doe");
        assertThat(response.getBody().email()).isEqualTo("john@example.com");
        assertThat(response.getBody().createdAt()).isNotNull();

        // Verify user exists in database
        Optional<User> savedUser = userRepository.findByEmail("john@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void createUser_WithDuplicateEmail_ReturnsBadRequest() {
        // Given - create first user
        CreateUserRequest firstUser = new CreateUserRequest(
                "First User",
                "duplicate@example.com",
                "password123"
        );
        restTemplate.postForEntity("/api/users", firstUser, UserResponse.class);

        // When - try to create second user with same email
        CreateUserRequest secondUser = new CreateUserRequest(
                "Second User",
                "duplicate@example.com",
                "password456"
        );
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/users",
                secondUser,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getUserById_WithValidId_ReturnsUser() {
        // Given - create a user and get auth token
        String token = getAuthToken("getbyid@example.com", "password123", "Get By Id User");
        Long userId = userRepository.findByEmail("getbyid@example.com").get().getId();

        // When
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/api/users/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                UserResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(userId);
        assertThat(response.getBody().email()).isEqualTo("getbyid@example.com");
    }

    @Test
    void updateUser_WithValidData_ReturnsUpdatedUser() {
        // Given - create a user and get auth token
        String token = getAuthToken("update@example.com", "password123", "Original Name");
        Long userId = userRepository.findByEmail("update@example.com").get().getId();

        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Updated Name",
                null,
                null
        );

        // When
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/api/users/" + userId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, authHeaders(token)),
                UserResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Updated Name");
        assertThat(response.getBody().email()).isEqualTo("update@example.com");
    }

    @Test
    void deleteUser_WithValidId_ReturnsNoContent() {
        // Given - create a user and get auth token
        String token = getAuthToken("delete@example.com", "password123", "Delete User");
        Long userId = userRepository.findByEmail("delete@example.com").get().getId();

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/users/" + userId,
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(token)),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify user is deleted from database
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void getAllUsers_WithAuthentication_ReturnsUsersList() {
        // Given - create users and get auth token
        String token = getAuthToken("user1@example.com", "password123", "User One");

        // Create another user
        CreateUserRequest secondUser = new CreateUserRequest(
                "User Two",
                "user2@example.com",
                "password123"
        );
        restTemplate.postForEntity("/api/users", secondUser, UserResponse.class);

        // When
        ResponseEntity<List> response = restTemplate.exchange(
                "/api/users",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                List.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(2);
    }
}
