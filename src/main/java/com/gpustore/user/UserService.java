package com.gpustore.user;

import com.gpustore.common.exception.ResourceNotFoundException;
import com.gpustore.common.exception.ValidationException;
import com.gpustore.user.dto.CreateUserRequest;
import com.gpustore.user.dto.UpdateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for user management operations.
 *
 * <p>Handles business logic for user CRUD operations including
 * password encoding, email uniqueness validation, and entity mapping.</p>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a new UserService with required dependencies.
     *
     * @param userRepository  the repository for user persistence
     * @param passwordEncoder the encoder for hashing passwords
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user with the provided details.
     *
     * <p>The password is encoded using BCrypt before storage.</p>
     *
     * @param request the user creation request
     * @return the created user entity
     * @throws ValidationException if the email already exists
     */
    public User create(CreateUserRequest request) {
        log.debug("Creating user with email: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Attempt to create user with existing email: {}", request.email());
            throw new ValidationException("Email already exists");
        }

        User user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password())
        );

        User savedUser = userRepository.save(user);
        log.debug("User created with id: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * Retrieves all users.
     *
     * @return a list of all users
     */
    @Transactional(readOnly = true)
    public List<User> findAll() {
        log.debug("Finding all users");
        return userRepository.findAll();
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id the user ID
     * @return the user entity
     * @throws ResourceNotFoundException if no user is found with the given ID
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        log.debug("Finding user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address
     * @return the user entity
     * @throws ResourceNotFoundException if no user is found with the given email
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /**
     * Updates an existing user with the provided details.
     *
     * <p>Only non-null fields in the request are updated. If email is changed,
     * uniqueness is verified. Password is re-encoded if changed.</p>
     *
     * @param id      the user ID
     * @param request the update request
     * @return the updated user entity
     * @throws ResourceNotFoundException if no user is found with the given ID
     * @throws ValidationException       if the new email already exists
     */
    public User update(Long id, UpdateUserRequest request) {
        log.debug("Updating user with id: {}", id);
        User user = findById(id);

        if (request.name() != null) {
            user.setName(request.name());
        }

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                log.warn("Attempt to update user {} with existing email: {}", id, request.email());
                throw new ValidationException("Email already exists");
            }
            user.setEmail(request.email());
        }

        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        User savedUser = userRepository.save(user);
        log.debug("User updated: id={}", id);
        return savedUser;
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id the user ID
     * @throws ResourceNotFoundException if no user is found with the given ID
     */
    public void delete(Long id) {
        log.debug("Deleting user with id: {}", id);
        User user = findById(id);
        userRepository.delete(user);
        log.debug("User deleted: id={}", id);
    }
}
