# PRP Part 1: Project Setup & Infrastructure (FULL DETAIL)

**Feature:** GPU E-commerce Platform - Project Initialization
**Confidence Score:** 9/10
**Estimated Files:** 12 files

---

## Context & Research

### Documentation URLs
- [Spring Boot 3.4 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes)
- [Spring Initializr](https://start.spring.io/)
- [JJWT GitHub](https://github.com/jwtk/jjwt)
- [SpringDoc OpenAPI](https://springdoc.org/)
- [Testcontainers Spring Boot Guide](https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/)
- [Flyway PostgreSQL Documentation](https://documentation.red-gate.com/fd/postgresql-database-277579325.html)

### Library Versions (Verified January 2026)
| Library | Version | Notes |
|---------|---------|-------|
| Spring Boot | 3.4.1 | Latest stable 3.x LTS |
| Java | 21 | LTS with Virtual Threads |
| JJWT | 0.12.6 | Split artifacts (api/impl/jackson) |
| SpringDoc OpenAPI | 2.7.0 | For Spring Boot 3.x |
| Testcontainers | 1.20.4 | With @ServiceConnection support |
| Flyway | 10.x | Managed by Spring Boot, requires flyway-database-postgresql |
| PostgreSQL Driver | 42.7.x | Managed by Spring Boot |

### Gotchas & Warnings
1. **Flyway 10+ requires separate PostgreSQL module** - Must include `flyway-database-postgresql` dependency
2. **JJWT requires runtime dependencies** - `jjwt-impl` and `jjwt-jackson` must be runtime scope
3. **Spring Boot 3.4 deprecates @MockBean** - Use `@MockitoBean` instead in tests
4. **Virtual Threads** - Enable via `spring.threads.virtual.enabled=true`
5. **JPA Auditing** - Requires `@EnableJpaAuditing` on configuration class

---

## Implementation Blueprint

### Task 1: Initialize Maven Project

**Goal:** Create Spring Boot 3.4.1 project with all required dependencies

**Approach:**
1. Generate project structure using Spring Initializr configuration
2. Create pom.xml with exact dependency versions
3. Generate Maven Wrapper files

**File: `pom.xml`**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.1</version>
        <relativePath/>
    </parent>

    <groupId>com.gpustore</groupId>
    <artifactId>gpu-ecommerce-platform</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>gpu-ecommerce-platform</name>
    <description>GPU E-commerce Platform Backend API</description>

    <properties>
        <java.version>21</java.version>
        <jjwt.version>0.12.6</jjwt.version>
        <springdoc.version>2.7.0</springdoc.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- API Documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### Task 2: Create Docker Compose for PostgreSQL

**File: `docker-compose.yml`**
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

---

### Task 3: Create Application Configuration

**File: `src/main/resources/application.yml`**
```yaml
spring:
  application:
    name: gpu-ecommerce-platform

  threads:
    virtual:
      enabled: true  # Enable Virtual Threads (Java 21)

  datasource:
    url: jdbc:postgresql://localhost:5432/gpustore
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

jwt:
  secret: ${JWT_SECRET:gpu-store-256-bit-secret-key-for-development-only-change-in-production-environment}
  expiration: 86400000  # 24 hours in milliseconds

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha

server:
  port: 8080
```

**File: `src/main/resources/application-test.yml`**
```yaml
spring:
  flyway:
    enabled: true
  jpa:
    hibernate:
      ddl-auto: validate

jwt:
  secret: test-secret-key-for-testing-only-256-bits-minimum-required
  expiration: 3600000  # 1 hour for tests
```

---

### Task 4: Create Main Application Class

**File: `src/main/java/com/gpustore/GpuEcommercePlatformApplication.java`**
```java
package com.gpustore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GpuEcommercePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(GpuEcommercePlatformApplication.class, args);
    }
}
```

---

### Task 5: Create Database Migrations

**File: `src/main/resources/db/migration/V1__create_schema.sql`**
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

**File: `src/main/resources/db/migration/V2__seed_gpu_products.sql`**
```sql
-- Seed GPU products (12 items)
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

### Task 6: Create Base Entity

**File: `src/main/java/com/gpustore/common/BaseEntity.java`**
```java
package com.gpustore.common;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```

---

### Task 7: Create Exception Classes

**File: `src/main/java/com/gpustore/common/exception/ResourceNotFoundException.java`**
```java
package com.gpustore.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s not found with id: %d", resourceName, id));
    }
}
```

**File: `src/main/java/com/gpustore/common/exception/ValidationException.java`**
```java
package com.gpustore.common.exception;

public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
```

**File: `src/main/java/com/gpustore/common/exception/ErrorResponse.java`**
```java
package com.gpustore.common.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
    int status,
    String message,
    LocalDateTime timestamp
) {
    public ErrorResponse(int status, String message) {
        this(status, message, LocalDateTime.now());
    }
}
```

**File: `src/main/java/com/gpustore/common/exception/GlobalExceptionHandler.java`**
```java
package com.gpustore.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            message
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Invalid email or password"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal server error"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

### Task 8: Create OpenAPI Configuration

**File: `src/main/java/com/gpustore/config/OpenApiConfig.java`**
```java
package com.gpustore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("GPU E-commerce Platform API")
                .version("1.0.0")
                .description("REST API for GPU E-commerce Platform - Users, Authentication, Products, and Orders")
                .contact(new Contact()
                    .name("GPU Store Team")
                    .email("support@gpustore.com")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token")));
    }
}
```

---

## Files Created in This Part

| # | File Path | Purpose |
|---|-----------|---------|
| 1 | `pom.xml` | Maven configuration with all dependencies |
| 2 | `docker-compose.yml` | PostgreSQL container setup |
| 3 | `src/main/resources/application.yml` | Main application configuration |
| 4 | `src/main/resources/application-test.yml` | Test profile configuration |
| 5 | `src/main/java/com/gpustore/GpuEcommercePlatformApplication.java` | Main application entry point |
| 6 | `src/main/resources/db/migration/V1__create_schema.sql` | Database schema migration |
| 7 | `src/main/resources/db/migration/V2__seed_gpu_products.sql` | Seed data migration |
| 8 | `src/main/java/com/gpustore/common/BaseEntity.java` | Base entity with auditing |
| 9 | `src/main/java/com/gpustore/common/exception/ResourceNotFoundException.java` | 404 exception |
| 10 | `src/main/java/com/gpustore/common/exception/ValidationException.java` | 400 exception |
| 11 | `src/main/java/com/gpustore/common/exception/ErrorResponse.java` | Error response record |
| 12 | `src/main/java/com/gpustore/common/exception/GlobalExceptionHandler.java` | Global exception handler |
| 13 | `src/main/java/com/gpustore/config/OpenApiConfig.java` | Swagger/OpenAPI configuration |

---

## Validation Gates

### Gate 1: Project Compiles
```bash
./mvnw clean compile
```
**Expected:** BUILD SUCCESS

### Gate 2: Docker Compose Starts
```bash
docker-compose up -d
docker-compose ps
```
**Expected:** gpustore-db container running, healthy

### Gate 3: Application Starts
```bash
./mvnw spring-boot:run
```
**Expected:**
- Application starts on port 8080
- Flyway migrations execute successfully
- No errors in console

### Gate 4: Swagger UI Accessible
```bash
curl http://localhost:8080/swagger-ui.html
```
**Expected:** Swagger UI HTML response (or redirect)

---

## Error Handling Strategy

| Error Type | HTTP Code | Handler Method |
|------------|-----------|----------------|
| Resource not found | 404 | `handleResourceNotFound` |
| Validation failure | 400 | `handleValidation`, `handleMethodArgumentNotValid` |
| Bad credentials | 401 | `handleBadCredentials` |
| Unexpected error | 500 | `handleGeneral` |

---

## Next Parts Preview

- **Part 2:** Security (JWT) + User Module + Auth Module (~12 files)
- **Part 3:** Product Module + Order Module (~14 files)
- **Part 4:** Integration Tests + README (~6 files)
