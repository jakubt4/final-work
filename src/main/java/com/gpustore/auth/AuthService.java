package com.gpustore.auth;

import com.gpustore.auth.dto.LoginResponse;
import com.gpustore.security.JwtTokenProvider;
import com.gpustore.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Service class for authentication operations.
 *
 * <p>Handles user authentication using Spring Security and JWT token generation.</p>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * Constructs a new AuthService with required dependencies.
     *
     * @param authenticationManager the Spring Security authentication manager
     * @param tokenProvider         the JWT token provider
     */
    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param email    the user's email address
     * @param password the user's password
     * @return a login response containing the JWT token
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     */
    public LoginResponse authenticate(String email, String password) {
        log.debug("Authenticating user: {}", email);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.generateToken(userPrincipal.getId(), userPrincipal.getEmail());
        log.debug("Token generated for user: id={}", userPrincipal.getId());

        return LoginResponse.of(token, tokenProvider.getExpirationMs());
    }
}
