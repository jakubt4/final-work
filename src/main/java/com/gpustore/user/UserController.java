package com.gpustore.user;

import com.gpustore.user.dto.CreateUserRequest;
import com.gpustore.user.dto.UpdateUserRequest;
import com.gpustore.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user management operations.
 *
 * <p>Provides endpoints for CRUD operations on users:</p>
 * <ul>
 *   <li>{@code POST /api/users} - Create a new user (public)</li>
 *   <li>{@code GET /api/users} - List all users</li>
 *   <li>{@code GET /api/users/{id}} - Get user by ID</li>
 *   <li>{@code PUT /api/users/{id}} - Update user</li>
 *   <li>{@code DELETE /api/users/{id}} - Delete user</li>
 * </ul>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    /**
     * Constructs a new UserController with the required service.
     *
     * @param userService the service for user operations
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a new user account.
     *
     * <p>This endpoint is public and allows user registration.</p>
     *
     * @param request the user creation request with name, email, and password
     * @return 201 Created with the created user details
     */
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.email());
        User user = userService.create(request);
        log.info("User created successfully: id={}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    /**
     * Retrieves all users.
     *
     * @return 200 OK with a list of all users
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        log.debug("Fetching all users");
        List<UserResponse> users = userService.findAll().stream()
                .map(UserResponse::from)
                .toList();
        log.debug("Found {} users", users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id the user ID
     * @return 200 OK with the user details, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        log.debug("Fetching user with id: {}", id);
        User user = userService.findById(id);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * Updates an existing user.
     *
     * @param id      the user ID
     * @param request the update request with optional name, email, and password
     * @return 200 OK with the updated user details, or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);
        User user = userService.update(id, request);
        log.info("User updated successfully: id={}", id);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id the user ID
     * @return 204 No Content on success, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        userService.delete(id);
        log.info("User deleted successfully: id={}", id);
        return ResponseEntity.noContent().build();
    }
}
