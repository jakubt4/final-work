package com.gpustore;

import com.gpustore.auth.dto.LoginRequest;
import com.gpustore.auth.dto.LoginResponse;
import com.gpustore.order.OrderRepository;
import com.gpustore.user.UserRepository;
import com.gpustore.user.dto.CreateUserRequest;
import com.gpustore.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests providing common test infrastructure.
 *
 * <p>Configures PostgreSQL and RabbitMQ containers via Testcontainers,
 * provides auth helpers and cleanup between tests.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("gpustore_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.12-management");

    @DynamicPropertySource
    static void configureRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected OrderRepository orderRepository;

    @BeforeEach
    void cleanUp() {
        // Clean orders first due to FK constraint
        orderRepository.deleteAll();
        userRepository.deleteAll();
        // Products remain (seed data from migrations)
    }

    protected String getAuthToken() {
        return getAuthToken("test@example.com", "password123", "Test User");
    }

    protected String getAuthToken(String email, String password, String name) {
        // Register user
        CreateUserRequest user = new CreateUserRequest(name, email, password);
        restTemplate.postForEntity("/api/users", user, UserResponse.class);

        // Login and get token
        LoginRequest login = new LoginRequest(email, password);
        ResponseEntity<LoginResponse> response =
                restTemplate.postForEntity("/api/auth/login", login, LoginResponse.class);

        return response.getBody().token();
    }

    protected HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAuthToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
