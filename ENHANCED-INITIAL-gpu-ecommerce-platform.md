# Enhanced System Context - GPU E-commerce Platform

## Codebase Status: GREENFIELD PROJECT

**Analysis Date:** 2026-01-17
**Current State:** No source code exists - full implementation required
**Existing Assets:** Documentation, workflow templates, .gitignore

---

# System Context

## Global Goal
Build a comprehensive **GPU E-commerce Platform** Backend API serving as a modern, production-ready service for selling graphics processing units. The platform provides complete lifecycle management for users, authentication, product catalog, and order processing.

## Scope
The service functions as a comprehensive Backend API featuring four distinct modules:
- **Users Module** - User identity and account management
- **Authentication Module** - JWT-based security and token management
- **Products Module** - GPU inventory and catalog management
- **Orders Module** - Transaction lifecycle and order processing

---

# Technology Standards

## Core Stack (Immutable Constraints)
| Component | Technology | Version/Constraint |
|-----------|------------|-------------------|
| Language | Java | 21 (LTS) - Must leverage Virtual Threads, Records for DTOs |
| Framework | Spring Boot | Latest stable (3.x) |
| Build Tool | Maven | Maven Wrapper (`mvnw`) required |
| Database | PostgreSQL | Via Docker Compose |
| DB Migrations | Flyway | Formal upgrade mechanism (NO raw `schema.sql`) |
| API Docs | OpenAPI/Swagger | Full endpoint documentation via SpringDoc |
| Testing | Testcontainers | Singleton pattern for integration tests |
| Security | JWT | Bearer Token authentication via JJWT library |

## Architectural Principles
- **Domain-Driven Design (DDD)** principles
- **Microservice-Ready Modular Monolith** architecture
- Clean separation between modules with clear boundaries
- Records for immutable DTOs
- Virtual Threads for enhanced concurrency

---

# Active Iteration Scope

## Current Focus: Part 1 - REST API Implementation
**Project Initialization, Core Modules (User/Auth/Product/Order), and DB Migration Setup**

> **IMPORTANT:** Part 1 and Part 2 must be implemented separately (as per `priloha-b-backend.md`). This iteration covers Part 1 only.

## Definition of Done - Part 1
- [ ] Maven project structure with Spring Boot 3.x configured
- [ ] Docker Compose setup for PostgreSQL
- [ ] Flyway migration tool configured with `src/main/resources/db/migration/`
- [ ] Initial migration scripts: `V1__create_schema.sql`
- [ ] Seed data migration: `V2__seed_gpu_products.sql` with 10+ GPU products
- [ ] All four core modules implemented with REST APIs
- [ ] JWT authentication protecting all endpoints (except public auth)
- [ ] OpenAPI/Swagger documentation accessible at `/swagger-ui.html`
- [ ] Integration tests with Testcontainers (minimum 5 test cases)
- [ ] README.md with setup and run instructions

---

# Project Structure (To Be Created)

```
gpu-ecommerce-platform/
├── .mvn/wrapper/                    # Maven Wrapper (auto-generated)
├── src/
│   ├── main/
│   │   ├── java/com/gpustore/
│   │   │   ├── GpuEcommercePlatformApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtConfig.java
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── common/
│   │   │   │   ├── BaseEntity.java
│   │   │   │   └── exception/
│   │   │   │       ├── GlobalExceptionHandler.java
│   │   │   │       ├── ResourceNotFoundException.java
│   │   │   │       └── ValidationException.java
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── UserPrincipal.java
│   │   │   ├── user/
│   │   │   │   ├── User.java                    # Entity
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── UserController.java
│   │   │   │   └── dto/
│   │   │   │       ├── CreateUserRequest.java   # Record
│   │   │   │       ├── UpdateUserRequest.java   # Record
│   │   │   │       └── UserResponse.java        # Record
│   │   │   ├── auth/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── AuthService.java
│   │   │   │   └── dto/
│   │   │   │       ├── LoginRequest.java        # Record
│   │   │   │       └── LoginResponse.java       # Record
│   │   │   ├── product/
│   │   │   │   ├── Product.java                 # Entity
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── ProductController.java
│   │   │   │   └── dto/
│   │   │   │       ├── CreateProductRequest.java
│   │   │   │       ├── UpdateProductRequest.java
│   │   │   │       └── ProductResponse.java
│   │   │   └── order/
│   │   │       ├── Order.java                   # Entity
│   │   │       ├── OrderItem.java               # Entity
│   │   │       ├── OrderStatus.java             # Enum
│   │   │       ├── OrderRepository.java
│   │   │       ├── OrderService.java
│   │   │       ├── OrderController.java
│   │   │       └── dto/
│   │   │           ├── CreateOrderRequest.java
│   │   │           ├── OrderItemRequest.java
│   │   │           ├── UpdateOrderRequest.java
│   │   │           └── OrderResponse.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-test.yml
│   │       └── db/migration/
│   │           ├── V1__create_schema.sql
│   │           └── V2__seed_gpu_products.sql
│   └── test/
│       └── java/com/gpustore/
│           ├── AbstractIntegrationTest.java     # Testcontainers base
│           ├── user/UserControllerIT.java
│           ├── auth/AuthControllerIT.java
│           ├── product/ProductControllerIT.java
│           └── order/OrderControllerIT.java
├── docker-compose.yml
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore                        # EXISTS - properly configured
└── README.md
```

---

# Functional Specifications

## Module 1: Users

### User Stories
- **US-1.1:** As a new user, I can register an account with name, email, and password
- **US-1.2:** As an admin, I can view, update, and delete user accounts
- **US-1.3:** As a user, I can view and update my profile information

### API Endpoints
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/users` | Register new user | No |
| GET | `/api/users` | List all users | Yes (JWT) |
| GET | `/api/users/{id}` | Get user by ID | Yes (JWT) |
| PUT | `/api/users/{id}` | Update user | Yes (JWT) |
| DELETE | `/api/users/{id}` | Delete user | Yes (JWT) |

### Implementation Pattern
```java
// Entity: src/main/java/com/gpustore/user/User.java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;  // BCrypt hashed
}

// DTO (Record): src/main/java/com/gpustore/user/dto/CreateUserRequest.java
public record CreateUserRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Email @Size(max = 100) String email,
    @NotBlank String password
) {}
```

### Validation Rules
- `name`: Required, max 100 characters
- `email`: Required, max 100 characters, must be unique, valid email format
- `password`: Required, string (BCrypt hashed before storage)

---

## Module 2: Authentication

### User Stories
- **US-2.1:** As a registered user, I can login with email and password to receive a JWT token
- **US-2.2:** As an authenticated user, my token is validated on each protected request

### API Endpoints
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/login` | Authenticate and get JWT | No |

### Implementation Pattern
```java
// DTO (Record): src/main/java/com/gpustore/auth/dto/LoginRequest.java
public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}

// DTO (Record): src/main/java/com/gpustore/auth/dto/LoginResponse.java
public record LoginResponse(
    String token,
    String type,  // "Bearer"
    Long expiresIn
) {}

// Security: src/main/java/com/gpustore/security/JwtTokenProvider.java
// - Generate JWT with user ID, email, roles
// - Validate and parse JWT tokens
// - Use JJWT library (io.jsonwebtoken)
```

### Token Specification
- Type: Bearer Token (JWT)
- Contains: User ID, email, roles, expiration
- Must be included in `Authorization: Bearer <token>` header
- Expiration: Configurable via `application.yml`

---

## Module 3: Products

### User Stories
- **US-3.1:** As a visitor, I can browse the GPU catalog
- **US-3.2:** As an admin, I can add, update, and remove products from inventory
- **US-3.3:** As a user, I can view product details including stock availability

### API Endpoints
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/products` | Create new product | Yes (JWT) |
| GET | `/api/products` | List all products | Yes (JWT) |
| GET | `/api/products/{id}` | Get product by ID | Yes (JWT) |
| PUT | `/api/products/{id}` | Update product | Yes (JWT) |
| DELETE | `/api/products/{id}` | Delete product | Yes (JWT) |

### Implementation Pattern
```java
// Entity: src/main/java/com/gpustore/product/Product.java
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;
}
```

### Validation Rules
- `name`: Required, max 100 characters
- `description`: Optional, text
- `price`: Required, must be >= 0
- `stock`: Required, must be >= 0
- `createdAt`: Auto-generated timestamp

---

## Module 4: Orders

### User Stories
- **US-4.1:** As a user, I can create an order with multiple products
- **US-4.2:** As a user, I can view my order history and order details
- **US-4.3:** As an admin, I can update order status through the lifecycle
- **US-4.4:** As the system, I prevent overselling by locking inventory during order creation

### API Endpoints
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/orders` | Create new order | Yes (JWT) |
| GET | `/api/orders` | List all orders | Yes (JWT) |
| GET | `/api/orders/{id}` | Get order by ID | Yes (JWT) |
| PUT | `/api/orders/{id}` | Update order | Yes (JWT) |
| DELETE | `/api/orders/{id}` | Delete/Cancel order | Yes (JWT) |

### Order Status Lifecycle
```
pending → processing → completed
    ↓
  expired
```

### Implementation Pattern
```java
// Entity: src/main/java/com/gpustore/order/Order.java
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}

// Enum: src/main/java/com/gpustore/order/OrderStatus.java
public enum OrderStatus {
    PENDING, PROCESSING, COMPLETED, EXPIRED
}

// Entity: src/main/java/com/gpustore/order/OrderItem.java
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;  // Captured at order time
}
```

### Validation Rules
- `userId`: Required, must reference existing user
- `total`: Calculated from items, must be >= 0
- `status`: Enum (PENDING, PROCESSING, COMPLETED, EXPIRED)
- `createdAt`: Auto-generated timestamp
- `updatedAt`: Auto-updated timestamp

### Order Items Validation
- `productId`: Required, must reference existing product
- `quantity`: Required, must be > 0
- `price`: Required, must be > 0 (captured at order time)

### Concurrency Control
```java
// OrderService - Pessimistic locking pattern
@Transactional
public Order createOrder(CreateOrderRequest request) {
    // Lock products to prevent overselling
    for (OrderItemRequest item : request.items()) {
        Product product = productRepository.findByIdWithLock(item.productId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStock() < item.quantity()) {
            throw new ValidationException("Insufficient stock for: " + product.getName());
        }

        product.setStock(product.getStock() - item.quantity());
    }
    // ... create order
}

// ProductRepository
@Query("SELECT p FROM Product p WHERE p.id = :id")
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Product> findByIdWithLock(@Param("id") Long id);
```

---

# Architecture & Data Model

## Entity Relationship Diagram

```
┌─────────────────┐       ┌─────────────────┐
│      USERS      │       │    PRODUCTS     │
├─────────────────┤       ├─────────────────┤
│ id (PK)         │       │ id (PK)         │
│ name (100)      │       │ name (100)      │
│ email (100, UQ) │       │ description     │
│ password        │       │ price (>=0)     │
│ created_at      │       │ stock (>=0)     │
│ updated_at      │       │ created_at      │
└────────┬────────┘       │ updated_at      │
         │                └────────┬────────┘
         │ 1:N                     │ 1:N
         ▼                         ▼
┌─────────────────┐       ┌─────────────────┐
│     ORDERS      │       │   ORDER_ITEMS   │
├─────────────────┤       ├─────────────────┤
│ id (PK)         │◄──────│ id (PK)         │
│ user_id (FK)    │  1:N  │ order_id (FK)   │
│ total (>=0)     │       │ product_id (FK) │
│ status (ENUM)   │       │ quantity (>0)   │
│ created_at      │       │ price (>0)      │
│ updated_at      │       └─────────────────┘
└─────────────────┘
```

## Database Migrations

### V1__create_schema.sql
Location: `src/main/resources/db/migration/V1__create_schema.sql`

```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    stock INTEGER NOT NULL CHECK (stock >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Orders table
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    total DECIMAL(10,2) NOT NULL CHECK (total >= 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'EXPIRED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Order items table
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price DECIMAL(10,2) NOT NULL CHECK (price > 0)
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
```

### V2__seed_gpu_products.sql
Location: `src/main/resources/db/migration/V2__seed_gpu_products.sql`

```sql
-- Seed GPU products (10+ items)
INSERT INTO products (name, description, price, stock) VALUES
('NVIDIA GeForce RTX 4090', 'Flagship GPU with 24GB GDDR6X, Ada Lovelace architecture', 1599.99, 15),
('NVIDIA GeForce RTX 4080 Super', '16GB GDDR6X, excellent 4K gaming performance', 999.99, 25),
('NVIDIA GeForce RTX 4070 Ti Super', '16GB GDDR6X, great value for enthusiasts', 799.99, 30),
('NVIDIA GeForce RTX 4070 Super', '12GB GDDR6X, solid 1440p gaming', 599.99, 40),
('NVIDIA GeForce RTX 4060 Ti', '8GB GDDR6, mainstream gaming champion', 399.99, 50),
('AMD Radeon RX 7900 XTX', '24GB GDDR6, RDNA 3 flagship', 949.99, 20),
('AMD Radeon RX 7900 XT', '20GB GDDR6, excellent 4K performance', 749.99, 25),
('AMD Radeon RX 7800 XT', '16GB GDDR6, strong 1440p contender', 499.99, 35),
('AMD Radeon RX 7700 XT', '12GB GDDR6, budget-friendly 1440p', 449.99, 40),
('AMD Radeon RX 7600', '8GB GDDR6, 1080p gaming value king', 269.99, 60),
('Intel Arc A770', '16GB GDDR6, competitive 1440p gaming', 349.99, 45),
('Intel Arc A750', '8GB GDDR6, budget 1080p gaming', 249.99, 55);
```

---

# Configuration Files

## application.yml
Location: `src/main/resources/application.yml`

```yaml
spring:
  application:
    name: gpu-ecommerce-platform

  datasource:
    url: jdbc:postgresql://localhost:5432/gpustore
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate  # Flyway handles schema
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here-change-in-production}
  expiration: 86400000  # 24 hours in milliseconds

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
```

## docker-compose.yml
Location: `docker-compose.yml`

```yaml
version: '3.8'

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

volumes:
  postgres_data:
```

## pom.xml Dependencies
Location: `pom.xml`

Key dependencies:
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `springdoc-openapi-starter-webmvc-ui` (2.x)
- `postgresql`
- `flyway-core`
- `flyway-database-postgresql`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- `spring-boot-starter-test`
- `spring-security-test`
- `testcontainers` (postgresql module)

---

# Common Patterns

## BaseEntity
Location: `src/main/java/com/gpustore/common/BaseEntity.java`

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Getters and setters
}
```

## GlobalExceptionHandler
Location: `src/main/java/com/gpustore/common/exception/GlobalExceptionHandler.java`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(500).body(new ErrorResponse(500, "Internal server error"));
    }
}

public record ErrorResponse(int status, String message) {}
```

---

# Testing Strategy

## Testcontainers Singleton Pattern
Location: `src/test/java/com/gpustore/AbstractIntegrationTest.java`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("gpustore_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    protected String getAuthToken(String email, String password) {
        LoginRequest login = new LoginRequest(email, password);
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
            "/api/auth/login", login, LoginResponse.class);
        return response.getBody().token();
    }

    protected HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
```

## Required Test Cases (Minimum 5)
1. **User Registration** - `POST /api/users` creates user successfully
2. **Authentication Flow** - `POST /api/auth/login` returns valid JWT
3. **Product CRUD** - Create, read, update, delete product
4. **Order Creation with Stock Validation** - Order reduces product stock
5. **Unauthorized Access Rejection** - Protected endpoints return 401 without token

---

# HTTP Error States

| Code | Status | Trigger |
|------|--------|---------|
| 400 | Bad Request | Validation failure (DTO constraints via `@Valid`) |
| 401 | Unauthorized | Invalid/Missing JWT Token (Spring Security) |
| 404 | Not Found | Resource does not exist (`ResourceNotFoundException`) |
| 500 | Internal Server Error | Unexpected server failure |

---

# Implementation Roadmap

## Phase 1: Project Setup
- [ ] Initialize Maven project with Spring Boot 3.x via Spring Initializr
- [ ] Configure `pom.xml` with all dependencies
- [ ] Create `docker-compose.yml` for PostgreSQL
- [ ] Configure `application.yml` with database and JWT settings
- [ ] Setup Flyway directory structure: `src/main/resources/db/migration/`

## Phase 2: Database Migrations
- [ ] Create `V1__create_schema.sql`: users, products, orders, order_items tables
- [ ] Create `V2__seed_gpu_products.sql`: 10+ GPU products

## Phase 3: Core Domain Implementation
- [ ] Create `BaseEntity` with common fields (id, createdAt, updatedAt)
- [ ] Implement User entity, repository, service, controller
- [ ] Implement Product entity, repository, service, controller
- [ ] Implement Order/OrderItem entities, repositories, services, controller
- [ ] Implement all DTOs as Java Records

## Phase 4: Security Implementation
- [ ] Configure Spring Security with JWT (`SecurityConfig`)
- [ ] Implement JWT token generation and validation (`JwtTokenProvider`)
- [ ] Create `JwtAuthenticationFilter`
- [ ] Create Authentication controller (`AuthController`)
- [ ] Secure endpoints: only `/api/users` (POST) and `/api/auth/login` are public

## Phase 5: API Standards
- [ ] Implement `GlobalExceptionHandler`
- [ ] Configure standard error responses (400, 401, 404, 500)
- [ ] Add input validation with Bean Validation (`@Valid`, `@NotBlank`, etc.)
- [ ] Configure OpenAPI/Swagger with SpringDoc

## Phase 6: Testing
- [ ] Configure Testcontainers with singleton pattern
- [ ] Write integration test: User registration
- [ ] Write integration test: Authentication flow
- [ ] Write integration test: Product CRUD
- [ ] Write integration test: Order creation with stock validation
- [ ] Write integration test: Unauthorized access rejection

## Phase 7: Documentation
- [ ] Create README.md with:
  - Project overview
  - Prerequisites (Java 21, Docker)
  - How to run Docker Compose for PostgreSQL
  - How to run database migrations (automatic via Flyway)
  - How to start the application (`./mvnw spring-boot:run`)
  - API documentation access: `http://localhost:8080/swagger-ui.html`

---

# Gap Analysis & Notes

## Architectural Compliance: VERIFIED
- Modular Monolith structure with clear module boundaries (user, auth, product, order)
- Java 21 LTS with Records for DTOs and Virtual Threads capability
- Spring Boot 3.x compatible

## Security Considerations
- Password hashing: Use `BCryptPasswordEncoder` (Spring Security default)
- JWT secret: Must be externalized via environment variable in production
- No role-based authorization in Part 1 (can be added if needed)

## Edge Cases to Handle
1. **Duplicate email registration** - Return 400 with clear message
2. **Order with out-of-stock product** - Return 400, do not partial-create
3. **Delete user with orders** - Decide: soft delete or cascade? (Recommend: prevent deletion)
4. **Update order after completion** - Prevent status regression

## Future Part 2 Considerations (Not in this iteration)
- RabbitMQ integration for event-driven architecture
- OrderCreated, OrderCompleted, OrderExpired events
- Background job for order expiration (every 60 seconds)
- Notifications table and audit trail

---

# Existing Assets Reference

| Asset | Location | Status |
|-------|----------|--------|
| .gitignore | `D:\dev\workshop\final-work\.gitignore` | EXISTS - Maven/IDE configured |
| INITIAL.md | `D:\dev\workshop\final-work\INITIAL.md` | EXISTS - Base specification |
| priloha-b-backend.md | `D:\dev\workshop\final-work\priloha-b-backend.md` | EXISTS - Detailed requirements |
| workflow.md | `D:\dev\workshop\final-work\workflow.md` | EXISTS - AI workflow guide |
| Workflow commands | `D:\dev\workshop\final-work\.claude\commands\` | EXISTS - enhance-init, generate-prp, execute-prp |
