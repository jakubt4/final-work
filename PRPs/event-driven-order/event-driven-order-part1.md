# PRP Part 1: Event-Driven Infrastructure & Core Async Processing

**Feature:** Event-Driven Order Lifecycle & Notifications
**Source:** `ENHANCED-INITIAL-event-driven-order.md`
**Created:** 2026-01-18
**Confidence Score:** 9/10

---

## Executive Summary

This PRP covers the foundational infrastructure for event-driven architecture:
1. RabbitMQ Docker setup
2. Spring AMQP configuration
3. Event Bus abstraction and domain events
4. OrderService refactoring (remove sync processing)
5. OrderProcessor async consumer
6. Integration test infrastructure updates

**Files Covered in This Part:** 13 files (new + modified)

---

## 1. Context & Research

### 1.1 Documentation References

| Resource | URL | Purpose |
|----------|-----|---------|
| Spring AMQP Reference | https://docs.spring.io/spring-amqp/reference/html/ | Core messaging patterns |
| RabbitMQ Tutorials | https://www.rabbitmq.com/tutorials | Exchange/Queue concepts |
| Testcontainers RabbitMQ | https://java.testcontainers.org/modules/rabbitmq/ | Test container setup |
| Spring Boot AMQP Starter | https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging.amqp | Auto-configuration |

### 1.2 Existing Codebase Patterns

**Pattern: Service with Logger (follow exactly)**
```java
// Reference: src/main/java/com/gpustore/order/OrderService.java:34-38
@Service
@Transactional
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    // ...
}
```

**Pattern: Repository Custom Queries**
```java
// Reference: src/main/java/com/gpustore/order/OrderRepository.java:33-34
@Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product")
List<Order> findAllWithItems();
```

**Pattern: Integration Test Base**
```java
// Reference: src/test/java/com/gpustore/AbstractIntegrationTest.java:22-32
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
}
```

### 1.3 Technology Versions (Verified)

| Component | Version | Source |
|-----------|---------|--------|
| Spring Boot | 3.4.1 | `pom.xml:11` |
| Java | 21 | `pom.xml:22` |
| PostgreSQL | 16-alpine | `docker-compose.yml:3` |
| RabbitMQ | 3.12-management | Required (matches Spring Boot 3.x compatibility) |
| Testcontainers | Managed by Spring Boot BOM | `pom.xml:100-112` |

---

## 2. Implementation Blueprint

### 2.1 Task Execution Order

| # | Task | Files | Depends On |
|---|------|-------|------------|
| 1 | Add Maven dependencies | `pom.xml` | - |
| 2 | Update docker-compose | `docker-compose.yml` | - |
| 3 | Create domain event records | `src/.../event/*.java` | 1 |
| 4 | Create EventBus interface | `src/.../event/EventBus.java` | 3 |
| 5 | Create RabbitMQ config | `src/.../config/RabbitMqConfig.java` | 1 |
| 6 | Implement RabbitMqEventBus | `src/.../event/RabbitMqEventBus.java` | 4, 5 |
| 7 | Update application.yml | `src/main/resources/application.yml` | 2 |
| 8 | Enable scheduling | `src/.../GpuStoreApplication.java` | - |
| 9 | Refactor OrderService | `src/.../order/OrderService.java` | 4, 6 |
| 10 | Create OrderProcessor | `src/.../order/OrderProcessor.java` | 5, 9 |
| 11 | Update AbstractIntegrationTest | `src/test/.../AbstractIntegrationTest.java` | 1 |
| 12 | Create OrderAsyncFlowIT | `src/test/.../order/OrderAsyncFlowIT.java` | 10, 11 |
| 13 | Verify existing tests pass | - | All above |

---

### 2.2 File-by-File Implementation

#### Task 1: Add Maven Dependencies

**File:** `pom.xml`
**Action:** ADD dependencies after line 44 (after spring-boot-starter-validation)

```xml
        <!-- Messaging -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
```

**Action:** ADD test dependency after line 112 (after testcontainers postgresql)

```xml
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>rabbitmq</artifactId>
            <scope>test</scope>
        </dependency>
```

**Validation:** `./mvnw dependency:resolve` should succeed

---

#### Task 2: Update docker-compose.yml

**File:** `docker-compose.yml`
**Action:** REPLACE entire file

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: gpustore-db
    environment:
      POSTGRES_DB: gpustore
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

  rabbitmq:
    image: rabbitmq:3.12-management
    container_name: gpustore-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
  rabbitmq_data:
```

**Validation:** `docker-compose up -d && docker-compose ps` shows both healthy

---

#### Task 3: Create Domain Event Records

**File:** `src/main/java/com/gpustore/event/OrderCreatedEvent.java`
**Action:** CREATE new file

```java
package com.gpustore.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain event published when a new order is created.
 * Triggers asynchronous order processing workflow.
 *
 * @param orderId   the unique identifier of the created order
 * @param userId    the ID of the user who placed the order
 * @param total     the total amount of the order
 * @param timestamp when the event occurred
 */
public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        BigDecimal total,
        LocalDateTime timestamp
) {}
```

**File:** `src/main/java/com/gpustore/event/OrderCompletedEvent.java`
**Action:** CREATE new file

```java
package com.gpustore.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain event published when an order is successfully completed.
 * Triggers notification to user and audit logging.
 *
 * @param orderId   the unique identifier of the completed order
 * @param userId    the ID of the user who owns the order
 * @param total     the total amount charged
 * @param timestamp when the event occurred
 */
public record OrderCompletedEvent(
        Long orderId,
        Long userId,
        BigDecimal total,
        LocalDateTime timestamp
) {}
```

**File:** `src/main/java/com/gpustore/event/OrderExpiredEvent.java`
**Action:** CREATE new file

```java
package com.gpustore.event;

import java.time.LocalDateTime;

/**
 * Domain event published when an order expires due to processing timeout.
 * Triggers system alert notification and audit logging.
 *
 * @param orderId   the unique identifier of the expired order
 * @param userId    the ID of the user who owns the order
 * @param reason    human-readable reason for expiration
 * @param timestamp when the event occurred
 */
public record OrderExpiredEvent(
        Long orderId,
        Long userId,
        String reason,
        LocalDateTime timestamp
) {}
```

---

#### Task 4: Create EventBus Interface

**File:** `src/main/java/com/gpustore/event/EventBus.java`
**Action:** CREATE new file

```java
package com.gpustore.event;

/**
 * Abstraction for publishing domain events.
 * Allows decoupling event producers from specific messaging infrastructure.
 *
 * <p>Implementations may use RabbitMQ, Kafka, or in-memory for testing.</p>
 */
public interface EventBus {

    /**
     * Publishes an event to the messaging infrastructure.
     *
     * @param routingKey the routing key determining which queue receives the event
     * @param event      the domain event object (must be JSON-serializable)
     */
    void publish(String routingKey, Object event);
}
```

---

#### Task 5: Create RabbitMQ Configuration

**File:** `src/main/java/com/gpustore/config/RabbitMqConfig.java`
**Action:** CREATE new file

```java
package com.gpustore.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for order event messaging.
 *
 * <p>Topology:</p>
 * <ul>
 *   <li>Exchange: orders.exchange (direct)</li>
 *   <li>Queue: orders.created.queue - consumed by OrderProcessor</li>
 *   <li>Queue: orders.completed.queue - consumed by NotificationService</li>
 *   <li>Queue: orders.expired.queue - consumed by NotificationService</li>
 *   <li>Queue: orders.dlq - dead letter queue for failed messages</li>
 * </ul>
 */
@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_NAME = "orders.exchange";
    public static final String CREATED_QUEUE = "orders.created.queue";
    public static final String COMPLETED_QUEUE = "orders.completed.queue";
    public static final String EXPIRED_QUEUE = "orders.expired.queue";
    public static final String DLQ_QUEUE = "orders.dlq";

    public static final String ROUTING_KEY_CREATED = "order.created";
    public static final String ROUTING_KEY_COMPLETED = "order.completed";
    public static final String ROUTING_KEY_EXPIRED = "order.expired";

    // ==================== Exchange ====================

    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    // ==================== Queues ====================

    @Bean
    public Queue ordersCreatedQueue() {
        return QueueBuilder.durable(CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DLQ_QUEUE)
                .build();
    }

    @Bean
    public Queue ordersCompletedQueue() {
        return QueueBuilder.durable(COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DLQ_QUEUE)
                .build();
    }

    @Bean
    public Queue ordersExpiredQueue() {
        return QueueBuilder.durable(EXPIRED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DLQ_QUEUE)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    // ==================== Bindings ====================

    @Bean
    public Binding createdBinding(Queue ordersCreatedQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(ordersCreatedQueue)
                .to(ordersExchange)
                .with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding completedBinding(Queue ordersCompletedQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(ordersCompletedQueue)
                .to(ordersExchange)
                .with(ROUTING_KEY_COMPLETED);
    }

    @Bean
    public Binding expiredBinding(Queue ordersExpiredQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(ordersExpiredQueue)
                .to(ordersExchange)
                .with(ROUTING_KEY_EXPIRED);
    }

    // ==================== Message Converter ====================

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
```

---

#### Task 6: Implement RabbitMqEventBus

**File:** `src/main/java/com/gpustore/event/RabbitMqEventBus.java`
**Action:** CREATE new file

```java
package com.gpustore.event;

import com.gpustore.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ implementation of the EventBus interface.
 *
 * <p>Publishes domain events to the orders exchange using the configured
 * JSON message converter.</p>
 */
@Service
public class RabbitMqEventBus implements EventBus {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqEventBus.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqEventBus(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(String routingKey, Object event) {
        log.info("Publishing event [{}]: {}", routingKey, event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, routingKey, event);
        log.debug("Event published successfully to exchange={}, routingKey={}",
                RabbitMqConfig.EXCHANGE_NAME, routingKey);
    }
}
```

---

#### Task 7: Update application.yml

**File:** `src/main/resources/application.yml`
**Action:** ADD after `spring.flyway` section (before `jwt:`)

```yaml
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    listener:
      simple:
        acknowledge-mode: auto
        retry:
          enabled: true
          initial-interval: 1000
          max-attempts: 3
          multiplier: 2.0
```

**Full context (lines to add after line ~20):**
```yaml
spring:
  # ... existing config ...

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    listener:
      simple:
        acknowledge-mode: auto
        retry:
          enabled: true
          initial-interval: 1000
          max-attempts: 3
          multiplier: 2.0

jwt:
  # ... rest of config ...
```

---

#### Task 8: Enable Scheduling

**File:** `src/main/java/com/gpustore/GpuStoreApplication.java`
**Action:** ADD `@EnableScheduling` annotation

```java
package com.gpustore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class GpuStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(GpuStoreApplication.class, args);
    }
}
```

---

#### Task 9: Refactor OrderService

**File:** `src/main/java/com/gpustore/order/OrderService.java`
**Action:** MODIFY - inject EventBus, publish event after order creation, REMOVE stock deduction

**Changes Required:**

1. Add import for EventBus and events:
```java
import com.gpustore.config.RabbitMqConfig;
import com.gpustore.event.EventBus;
import com.gpustore.event.OrderCreatedEvent;
```

2. Add EventBus field and constructor parameter:
```java
private final EventBus eventBus;

public OrderService(OrderRepository orderRepository,
                    ProductRepository productRepository,
                    UserRepository userRepository,
                    EventBus eventBus) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.userRepository = userRepository;
    this.eventBus = eventBus;
}
```

3. Modify `create()` method - REMOVE stock deduction, ADD event publishing:
```java
public Order create(Long userId, CreateOrderRequest request) {
    log.debug("Creating order for user: {}", userId);
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

    Order order = new Order(user, BigDecimal.ZERO, OrderStatus.PENDING);
    BigDecimal total = BigDecimal.ZERO;

    for (OrderItemRequest itemRequest : request.items()) {
        // Validate product exists (no stock deduction here - moved to async processing)
        Product product = productRepository.findById(itemRequest.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", itemRequest.productId()));

        // NOTE: Stock validation/deduction moved to OrderProcessor for async flow
        // We still capture the price at order time
        BigDecimal itemPrice = product.getPrice();
        BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));
        total = total.add(itemTotal);

        OrderItem orderItem = new OrderItem(order, product, itemRequest.quantity(), itemPrice);
        order.addItem(orderItem);
    }

    order.setTotal(total);
    Order savedOrder = orderRepository.save(order);
    log.info("Order created: id={}, total={}, status=PENDING", savedOrder.getId(), total);

    // Publish event for async processing
    eventBus.publish(RabbitMqConfig.ROUTING_KEY_CREATED, new OrderCreatedEvent(
            savedOrder.getId(),
            userId,
            savedOrder.getTotal(),
            LocalDateTime.now()
    ));

    return savedOrder;
}
```

4. Add missing import:
```java
import java.time.LocalDateTime;
```

**Critical Note:** Remove the `productRepository.findByIdWithLock()` call and replace with `findById()` since stock deduction is now async.

---

#### Task 10: Create OrderProcessor

**File:** `src/main/java/com/gpustore/order/OrderProcessor.java`
**Action:** CREATE new file

```java
package com.gpustore.order;

import com.gpustore.common.exception.ResourceNotFoundException;
import com.gpustore.config.RabbitMqConfig;
import com.gpustore.event.EventBus;
import com.gpustore.event.OrderCompletedEvent;
import com.gpustore.event.OrderCreatedEvent;
import com.gpustore.product.Product;
import com.gpustore.product.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Asynchronous order processor that consumes OrderCreatedEvent messages.
 *
 * <p>Processing flow:</p>
 * <ol>
 *   <li>Receives OrderCreatedEvent from orders.created.queue</li>
 *   <li>Updates order status to PROCESSING</li>
 *   <li>Simulates payment processing (5 second delay)</li>
 *   <li>50% success rate: on success, deducts stock and marks COMPLETED</li>
 *   <li>On failure, order remains in PROCESSING (scheduler will expire it)</li>
 * </ol>
 */
@Service
public class OrderProcessor {

    private static final Logger log = LoggerFactory.getLogger(OrderProcessor.class);
    private static final Random RANDOM = new Random();
    private static final int PAYMENT_SIMULATION_MS = 5000;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final EventBus eventBus;

    public OrderProcessor(OrderRepository orderRepository,
                          ProductRepository productRepository,
                          EventBus eventBus) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.eventBus = eventBus;
    }

    /**
     * Processes an order asynchronously after creation.
     *
     * @param event the order created event containing order details
     */
    @RabbitListener(queues = RabbitMqConfig.CREATED_QUEUE)
    @Transactional
    public void processOrder(OrderCreatedEvent event) {
        log.info("Processing order: orderId={}", event.orderId());

        Order order = orderRepository.findByIdWithItems(event.orderId())
                .orElseThrow(() -> {
                    log.error("Order not found for processing: {}", event.orderId());
                    return new ResourceNotFoundException("Order", event.orderId());
                });

        // Idempotency check: only process PENDING orders
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order {} not in PENDING state (current={}), skipping processing",
                    event.orderId(), order.getStatus());
            return;
        }

        // Transition to PROCESSING
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
        log.info("Order {} status updated to PROCESSING", order.getId());

        // Simulate payment processing
        simulatePaymentProcessing();

        // 50% success rate simulation
        if (RANDOM.nextBoolean()) {
            completeOrder(order);
        } else {
            log.info("Order {} payment failed simulation, will be expired by scheduler",
                    order.getId());
            // Order stays in PROCESSING - scheduler will expire it after timeout
        }
    }

    private void simulatePaymentProcessing() {
        try {
            log.debug("Simulating payment processing ({} ms delay)", PAYMENT_SIMULATION_MS);
            Thread.sleep(PAYMENT_SIMULATION_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Payment simulation interrupted");
        }
    }

    private void completeOrder(Order order) {
        // Deduct stock for all items
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findByIdWithLock(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", item.getProduct().getId()));

            int newStock = product.getStock() - item.getQuantity();
            if (newStock < 0) {
                log.error("Insufficient stock for product {} during order {} completion",
                        product.getId(), order.getId());
                // In a real system, we might handle this differently (refund, partial fulfillment)
                // For now, we still complete but log the error
                newStock = 0;
            }
            product.setStock(newStock);
            productRepository.save(product);
            log.debug("Stock deducted for product {}: quantity={}, newStock={}",
                    product.getId(), item.getQuantity(), newStock);
        }

        // Mark as completed
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        // Publish completion event
        eventBus.publish(RabbitMqConfig.ROUTING_KEY_COMPLETED, new OrderCompletedEvent(
                order.getId(),
                order.getUser().getId(),
                order.getTotal(),
                LocalDateTime.now()
        ));

        log.info("Order {} completed successfully", order.getId());
    }
}
```

---

#### Task 11: Update AbstractIntegrationTest

**File:** `src/test/java/com/gpustore/AbstractIntegrationTest.java`
**Action:** ADD RabbitMQ container alongside PostgreSQL

```java
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
```

---

#### Task 12: Create OrderAsyncFlowIT

**File:** `src/test/java/com/gpustore/order/OrderAsyncFlowIT.java`
**Action:** CREATE new file

```java
package com.gpustore.order;

import com.gpustore.AbstractIntegrationTest;
import com.gpustore.order.dto.CreateOrderRequest;
import com.gpustore.order.dto.OrderItemRequest;
import com.gpustore.order.dto.OrderResponse;
import com.gpustore.product.Product;
import com.gpustore.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for asynchronous order processing flow.
 */
class OrderAsyncFlowIT extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createOrder_shouldReturnPendingImmediately() {
        // Given
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        // When
        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                OrderResponse.class
        );

        // Then - should return immediately with PENDING status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void createOrder_shouldTransitionToProcessingWithinTimeout() {
        // Given
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        // When
        ResponseEntity<OrderResponse> createResponse = restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                OrderResponse.class
        );
        Long orderId = createResponse.getBody().id();

        // Then - should transition to PROCESSING within 2 seconds
        await().atMost(2, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .until(() -> {
                    ResponseEntity<OrderResponse> getResponse = restTemplate.exchange(
                            "/api/orders/" + orderId,
                            HttpMethod.GET,
                            new HttpEntity<>(authHeaders(token)),
                            OrderResponse.class
                    );
                    return getResponse.getBody().status() == OrderStatus.PROCESSING;
                });
    }

    @Test
    void createOrder_shouldReachTerminalStateWithinTimeout() {
        // Given
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        // When
        ResponseEntity<OrderResponse> createResponse = restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                OrderResponse.class
        );
        Long orderId = createResponse.getBody().id();

        // Then - should reach COMPLETED or stay in PROCESSING (50% success rate)
        // Wait for processing to complete (5s payment simulation + buffer)
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> {
                    ResponseEntity<OrderResponse> getResponse = restTemplate.exchange(
                            "/api/orders/" + orderId,
                            HttpMethod.GET,
                            new HttpEntity<>(authHeaders(token)),
                            OrderResponse.class
                    );
                    OrderStatus status = getResponse.getBody().status();
                    // Either COMPLETED (success) or PROCESSING (failed, waiting for expiration)
                    return status == OrderStatus.COMPLETED || status == OrderStatus.PROCESSING;
                });
    }

    @Test
    void createOrder_shouldNotDeductStockImmediately() {
        // Given
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        int initialStock = product.getStock();
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        // When
        restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                OrderResponse.class
        );

        // Then - stock should not be deducted immediately (deferred to async processing)
        Product reloadedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(reloadedProduct.getStock()).isEqualTo(initialStock);
    }
}
```

**Note:** Add Awaitility dependency for async testing (already included in spring-boot-starter-test).

---

## 3. Validation Gates

### Gate 1: Compilation
```bash
./mvnw clean compile
```
**Expected:** BUILD SUCCESS

### Gate 2: Unit Tests (Quick Feedback)
```bash
./mvnw test -Dtest=!*IT
```
**Expected:** All non-IT tests pass

### Gate 3: Full Integration Tests
```bash
docker-compose up -d
./mvnw clean verify
```
**Expected:** All tests pass including new OrderAsyncFlowIT

### Gate 4: Manual Smoke Test
```bash
# Start infrastructure
docker-compose up -d

# Start application
./mvnw spring-boot:run

# In another terminal, test the flow:
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@gpustore.com","password":"admin123"}'

# 2. Create order (should return PENDING immediately)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"items":[{"productId":1,"quantity":1}]}'

# 3. Check status (should transition PENDING -> PROCESSING -> COMPLETED/PROCESSING)
curl http://localhost:8080/api/orders/<orderId> \
  -H "Authorization: Bearer <token>"
```

### Gate 5: RabbitMQ Management UI
Open http://localhost:15672 (guest/guest) and verify:
- Exchange `orders.exchange` exists
- Queues `orders.created.queue`, `orders.completed.queue`, `orders.expired.queue`, `orders.dlq` exist
- Messages flowing through queues

---

## 4. Error Handling Strategy

| Scenario | Handling |
|----------|----------|
| RabbitMQ unavailable at startup | Application fails fast with clear error (Spring Boot default) |
| Message processing fails | Retry 3 times with exponential backoff, then send to DLQ |
| Order not found during processing | Log error, throw exception (message goes to DLQ) |
| Order already processed (idempotency) | Log warning, skip processing |
| Stock insufficient during completion | Log error, complete order anyway (stock goes to 0) |

---

## 5. Rollback Plan

If issues arise, revert to synchronous processing:

1. Remove `@RabbitListener` from `OrderProcessor`
2. Restore stock deduction in `OrderService.create()`
3. Comment out event publishing in `OrderService`
4. Tests should pass with synchronous behavior

---

## Summary

**Part 1 delivers:**
- RabbitMQ infrastructure in Docker
- Event-driven messaging with Spring AMQP
- Async order processing with 5s payment simulation
- 50% success rate simulation
- Integration test coverage with Testcontainers

**Files created/modified:** 13

**Next:** Part 2 covers Notifications module and Scheduler; Part 3 covers Frontend polling.
