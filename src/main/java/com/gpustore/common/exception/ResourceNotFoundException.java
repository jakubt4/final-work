package com.gpustore.common.exception;

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * <p>This exception is handled by {@link GlobalExceptionHandler} and results
 * in a 404 Not Found HTTP response.</p>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified message.
     *
     * @param message the detail message describing the missing resource
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException for a specific resource type and ID.
     *
     * @param resourceName the type of resource that was not found (e.g., "User", "Product")
     * @param id           the ID of the resource that was not found
     */
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s not found with id: %d", resourceName, id));
    }
}
