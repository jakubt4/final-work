package com.gpustore.auth;

import com.gpustore.auth.dto.LoginRequest;
import com.gpustore.auth.dto.LoginResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations.
 *
 * <p>Provides endpoints for user authentication:</p>
 * <ul>
 *   <li>{@code POST /api/auth/login} - Authenticate user and receive JWT token</li>
 * </ul>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    /**
     * Constructs a new AuthController with the required service.
     *
     * @param authService the service for authentication operations
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request the login request containing email and password
     * @return 200 OK with JWT token on success, or 401 Unauthorized on failure
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());
        LoginResponse response = authService.authenticate(request.email(), request.password());
        log.info("Login successful for email: {}", request.email());
        return ResponseEntity.ok(response);
    }
}
