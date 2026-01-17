# PRP Part 4: Integration Tests & Documentation (To Be Expanded)

<!-- STATUS: OUTLINE ONLY - Run /execute-prp to expand with full implementation details -->

**Feature:** GPU E-commerce Platform - Testing & Documentation
**Estimated Files:** 6 files
**Dependencies:** Parts 1, 2, and 3 must be completed first

---

## Modules Covered

### 1. Test Infrastructure

- `src/test/java/com/gpustore/AbstractIntegrationTest.java`
  - Base class for all integration tests
  - Testcontainers PostgreSQL setup (singleton pattern)
  - @ServiceConnection for auto-configuration
  - Helper methods for authentication
  - TestRestTemplate configuration

---

### 2. Integration Tests (Minimum 5 Required)

- `src/test/java/com/gpustore/user/UserControllerIT.java`
  - **Test 1:** User registration creates user successfully
  - Test duplicate email returns 400
  - Test get user by ID returns user data
  - Test update user works correctly
  - Test delete user removes user

- `src/test/java/com/gpustore/auth/AuthControllerIT.java`
  - **Test 2:** Login with valid credentials returns JWT token
  - Test login with invalid password returns 401
  - Test login with non-existent email returns 401

- `src/test/java/com/gpustore/product/ProductControllerIT.java`
  - **Test 3:** Product CRUD operations work correctly
  - Test create product with valid data
  - Test get all products returns seeded data
  - Test update product modifies data
  - Test delete product removes product

- `src/test/java/com/gpustore/order/OrderControllerIT.java`
  - **Test 4:** Order creation reduces product stock
  - Test order with insufficient stock returns 400
  - Test order total calculated correctly
  - Test order status updates work

- `src/test/java/com/gpustore/security/SecurityIT.java`
  - **Test 5:** Protected endpoints return 401 without token
  - Test valid token grants access
  - Test expired token returns 401
  - Test malformed token returns 401

---

### 3. Documentation

- `README.md`
  - Project overview
  - Technology stack
  - Prerequisites (Java 21, Docker)
  - Quick start guide
  - API documentation link
  - Development setup
  - Testing instructions

---

## High-Level Requirements

### Test Configuration
- Use Testcontainers with PostgreSQL 16-alpine
- Singleton container pattern for performance
- @SpringBootTest with RANDOM_PORT
- @ServiceConnection for database auto-configuration
- Clean database between tests using @Sql or @Transactional

### Required Test Assertions
1. **User Registration:**
   - Response status 201 Created
   - Response body contains user ID
   - Password not returned in response
   - User exists in database

2. **Authentication:**
   - Response status 200 OK
   - Response contains valid JWT token
   - Token type is "Bearer"
   - Token can be used for protected endpoints

3. **Product CRUD:**
   - All 12 seed products returned on GET
   - Create returns 201 with product data
   - Update returns 200 with modified data
   - Delete returns 204 No Content

4. **Order with Stock:**
   - Order created with status PENDING
   - Product stock reduced by order quantity
   - Order total equals sum of (price * quantity)

5. **Security:**
   - GET /api/users without token returns 401
   - GET /api/users with valid token returns 200
   - POST /api/users (registration) works without token
   - POST /api/auth/login works without token

### README Structure
```markdown
# GPU E-commerce Platform

## Overview
Brief description of the platform.

## Technology Stack
- Java 21
- Spring Boot 3.4.1
- PostgreSQL 16
- JWT Authentication
- Flyway Migrations
- Testcontainers

## Prerequisites
- Java 21 JDK
- Docker & Docker Compose
- Maven 3.9+

## Quick Start
1. Start PostgreSQL: `docker-compose up -d`
2. Run application: `./mvnw spring-boot:run`
3. Access Swagger UI: http://localhost:8080/swagger-ui.html

## API Documentation
Available at `/swagger-ui.html` when application is running.

## Testing
Run tests: `./mvnw clean verify`

## API Endpoints
[Table of all endpoints with methods and auth requirements]
```

---

## Validation Gates

```bash
# Run all tests:
./mvnw clean verify

# Expected: BUILD SUCCESS with 5+ passing tests

# Check test report:
# target/surefire-reports/

# Verify README exists and is comprehensive:
cat README.md

# Final validation - full workflow:
docker-compose up -d
./mvnw spring-boot:run &
sleep 10
curl http://localhost:8080/swagger-ui.html
curl http://localhost:8080/api-docs
```

---

## Test Patterns

### Authentication Helper
```java
protected String getAuthToken() {
    // Register user
    CreateUserRequest user = new CreateUserRequest(
        "Test User", "test@example.com", "password123");
    restTemplate.postForEntity("/api/users", user, UserResponse.class);

    // Login and get token
    LoginRequest login = new LoginRequest("test@example.com", "password123");
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
```

### Database Cleanup
```java
@BeforeEach
void setUp() {
    // Clean orders first (FK constraint)
    orderRepository.deleteAll();
    userRepository.deleteAll();
    // Products remain (seed data)
}
```
