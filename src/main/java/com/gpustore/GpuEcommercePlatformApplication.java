package com.gpustore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for the GPU E-commerce Platform application.
 *
 * <p>This Spring Boot application provides a RESTful API for managing
 * GPU products, user accounts, authentication, and orders.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>JWT-based authentication and authorization</li>
 *   <li>Product catalog management with inventory tracking</li>
 *   <li>Order processing with status management</li>
 *   <li>User registration and profile management</li>
 * </ul>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class GpuEcommercePlatformApplication {

    /**
     * Application entry point.
     *
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(GpuEcommercePlatformApplication.class, args);
    }
}
