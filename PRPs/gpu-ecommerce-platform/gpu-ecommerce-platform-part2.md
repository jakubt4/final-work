# PRP Part 2: Security (JWT) + User & Auth Modules (To Be Expanded)

<!-- STATUS: OUTLINE ONLY - Run /execute-prp to expand with full implementation details -->

**Feature:** GPU E-commerce Platform - Security & Authentication
**Estimated Files:** 12 files
**Dependencies:** Part 1 must be completed first

---

## Modules Covered

### 1. Security Infrastructure
- `src/main/java/com/gpustore/security/JwtTokenProvider.java`
  - JWT token generation using JJWT 0.12.6
  - Token validation and parsing
  - Claims extraction (userId, email)

- `src/main/java/com/gpustore/security/JwtAuthenticationFilter.java`
  - OncePerRequestFilter implementation
  - Extract token from Authorization header
  - Set SecurityContext on valid token

- `src/main/java/com/gpustore/security/UserPrincipal.java`
  - UserDetails implementation
  - Wrap User entity for Spring Security

- `src/main/java/com/gpustore/config/SecurityConfig.java`
  - SecurityFilterChain configuration
  - Public endpoints: POST /api/users, POST /api/auth/login
  - All other endpoints require authentication
  - BCryptPasswordEncoder bean
  - CORS and CSRF configuration

---

### 2. User Module
- `src/main/java/com/gpustore/user/User.java`
  - Entity extending BaseEntity
  - Fields: name, email (unique), password (hashed)

- `src/main/java/com/gpustore/user/UserRepository.java`
  - JpaRepository<User, Long>
  - findByEmail(String email)
  - existsByEmail(String email)

- `src/main/java/com/gpustore/user/UserService.java`
  - CRUD operations
  - Password hashing with BCryptPasswordEncoder
  - Duplicate email validation

- `src/main/java/com/gpustore/user/UserController.java`
  - POST /api/users (public - registration)
  - GET /api/users (protected)
  - GET /api/users/{id} (protected)
  - PUT /api/users/{id} (protected)
  - DELETE /api/users/{id} (protected)

- `src/main/java/com/gpustore/user/dto/CreateUserRequest.java`
  - Record with @NotBlank, @Email, @Size validation

- `src/main/java/com/gpustore/user/dto/UpdateUserRequest.java`
  - Record for partial updates

- `src/main/java/com/gpustore/user/dto/UserResponse.java`
  - Record excluding password field

---

### 3. Auth Module
- `src/main/java/com/gpustore/auth/AuthService.java`
  - authenticate(email, password) method
  - Use AuthenticationManager
  - Return JWT token on success

- `src/main/java/com/gpustore/auth/AuthController.java`
  - POST /api/auth/login (public)
  - Return LoginResponse with token

- `src/main/java/com/gpustore/auth/dto/LoginRequest.java`
  - Record: email, password

- `src/main/java/com/gpustore/auth/dto/LoginResponse.java`
  - Record: token, type ("Bearer"), expiresIn

---

## High-Level Requirements

### Security Configuration
- JWT secret from application.yml (jwt.secret)
- Token expiration: 24 hours (configurable)
- BCrypt password hashing (strength 10)
- Stateless session management

### Validation Rules
- User name: @NotBlank, @Size(max=100)
- User email: @NotBlank, @Email, @Size(max=100), unique
- User password: @NotBlank, @Size(min=6)
- Login email: @NotBlank, @Email
- Login password: @NotBlank

### Error Handling
- Duplicate email: 400 Bad Request
- Invalid credentials: 401 Unauthorized
- User not found: 404 Not Found
- Missing/invalid token: 401 Unauthorized

---

## Validation Gates

```bash
# After implementation:
./mvnw clean compile

# Test user registration (no auth required):
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"password123"}'

# Test login:
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Test protected endpoint with token:
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <token>"

# Test protected endpoint without token (should return 401):
curl http://localhost:8080/api/users
```
