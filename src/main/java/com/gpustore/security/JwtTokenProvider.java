package com.gpustore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Provider for JWT token operations including generation, validation, and parsing.
 *
 * <p>Uses HMAC-SHA signing algorithm with a configurable secret key and expiration time.
 * Tokens contain the user ID as subject and email as a custom claim.</p>
 *
 * <p>Configuration properties:</p>
 * <ul>
 *   <li>{@code jwt.secret} - The secret key for signing tokens (min 256 bits)</li>
 *   <li>{@code jwt.expiration} - Token validity duration in milliseconds</li>
 * </ul>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey key;
    private final long expirationMs;

    /**
     * Constructs a new JwtTokenProvider with the specified secret and expiration.
     *
     * @param secret       the secret key for signing tokens
     * @param expirationMs the token validity duration in milliseconds
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a new JWT token for the specified user.
     *
     * @param userId the user's unique identifier
     * @param email  the user's email address
     * @return the generated JWT token string
     */
    public String generateToken(Long userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        log.debug("Generating token for user: id={}, email={}", userId, email);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Validates a JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid and not expired, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * @param token the JWT token to parse
     * @return the user ID stored in the token subject
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extracts the email address from a JWT token.
     *
     * @param token the JWT token to parse
     * @return the email address stored in the token claims
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("email", String.class);
    }

    /**
     * Returns the configured token expiration time.
     *
     * @return the token expiration time in milliseconds
     */
    public long getExpirationMs() {
        return expirationMs;
    }
}
