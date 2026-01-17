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
| DB Migrations | Flyway or Liquibase | Formal upgrade mechanism (NO raw `schema.sql`) |
| API Docs | OpenAPI/Swagger | Full endpoint documentation |
| Testing | Testcontainers | Singleton pattern for integration tests |
| Security | JWT | Bearer Token authentication |

## Architectural Principles
- **Domain-Driven Design (DDD)** principles
- **Microservice-Ready Modular Monolith** architecture
- Clean separation between modules with clear boundaries
- Records for immutable DTOs
- Virtual Threads for enhanced concurrency

---

# Active Iteration Scope

## Current Focus
**Project Initialization, Core Modules (User/Auth/Product/Order), and DB Migration Setup**

## Definition of Done
- [ ] Maven project structure with Spring Boot 3.x configured
- [ ] Docker Compose setup for PostgreSQL
- [ ] Database migration tool (Flyway/Liquibase) configured
- [ ] Initial migration scripts with schema creation
- [ ] Seed data migration with 10+ GPU products
- [ ] All four core modules implemented with REST APIs
- [ ] JWT authentication protecting all endpoints (except public auth)
- [ ] OpenAPI/Swagger documentation accessible
- [ ] Integration tests with Testcontainers (minimum 5 test cases)
- [ ] README.md with setup and run instructions

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
| GET | `/api/users` | List all users | Yes |
| GET | `/api/users/{id}` | Get user by ID | Yes |
| PUT | `/api/users/{id}` | Update user | Yes |
| DELETE | `/api/users/{id}` | Delete user | Yes |

### Validation Rules
- `name`: Required, max 100 characters
- `email`: Required, max 100 characters, must be unique, valid email format
- `password`: Required, string (hashed before storage)

---

## Module 2: Authentication

### User Stories
- **US-2.1:** As a registered user, I can login with email and password to receive a JWT token
- **US-2.2:** As an authenticated user, my token is validated on each protected request

### API Endpoints
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/login` | Authenticate and get JWT | No |

### Token Specification
- Type: Bearer Token (JWT)
- Contains: User ID, email, roles, expiration
- Must be included in `Authorization` header for protected endpoints

---

## Module 3: Products

### User Stories
- **US-3.1:** As a visitor, I can browse the GPU catalog
- **US-3.2:** As an admin, I can add, update, and remove products from inventory
- **US-3.3:** As a user, I can view product details including stock availability

### API Endpoints
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/products` | Create new product | Yes |
| GET | `/api/products` | List all products | Yes |
| GET | `/api/products/{id}` | Get product by ID | Yes |
| PUT | `/api/products/{id}` | Update product | Yes |
| DELETE | `/api/products/{id}` | Delete product | Yes |

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
| POST | `/api/orders` | Create new order | Yes |
| GET | `/api/orders` | List all orders | Yes |
| GET | `/api/orders/{id}` | Get order by ID | Yes |
| PUT | `/api/orders/{id}` | Update order | Yes |
| DELETE | `/api/orders/{id}` | Delete/Cancel order | Yes |

### Order Status Lifecycle
```
pending → processing → completed
    ↓
  expired
```

### Validation Rules
- `userId`: Required, must reference existing user
- `total`: Calculated, must be >= 0
- `status`: Enum (pending, processing, completed, expired)
- `createdAt`: Auto-generated timestamp
- `updatedAt`: Auto-updated timestamp

### Order Items Validation
- `productId`: Required, must reference existing product
- `quantity`: Required, must be > 0
- `price`: Required, must be > 0 (captured at order time)

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

## Database Schema

### users
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT/UUID | PRIMARY KEY, AUTO |
| name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(100) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW |

### products
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT/UUID | PRIMARY KEY, AUTO |
| name | VARCHAR(100) | NOT NULL |
| description | TEXT | NULLABLE |
| price | DECIMAL(10,2) | NOT NULL, CHECK >= 0 |
| stock | INTEGER | NOT NULL, CHECK >= 0 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW |

### orders
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT/UUID | PRIMARY KEY, AUTO |
| user_id | BIGINT/UUID | NOT NULL, FK → users(id) |
| total | DECIMAL(10,2) | NOT NULL, CHECK >= 0 |
| status | VARCHAR(20) | NOT NULL, ENUM CHECK |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW |

### order_items
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT/UUID | PRIMARY KEY, AUTO |
| order_id | BIGINT/UUID | NOT NULL, FK → orders(id) |
| product_id | BIGINT/UUID | NOT NULL, FK → products(id) |
| quantity | INTEGER | NOT NULL, CHECK > 0 |
| price | DECIMAL(10,2) | NOT NULL, CHECK > 0 |

## Authentication Strategy
- **JWT Bearer Token** authentication
- Token issued on successful login via `/api/auth/login`
- All endpoints protected except:
  - `POST /api/users` (registration)
  - `POST /api/auth/login` (authentication)
- Token validation via Spring Security filter chain

## Concurrency Control
- **Pessimistic Locking** on product stock during order creation
- Prevents overselling by locking inventory rows during transaction
- Transaction isolation level: SERIALIZABLE or SELECT FOR UPDATE

---

# Implementation Roadmap

## Phase 1: Project Setup
- [ ] Initialize Maven project with Spring Boot 3.x
- [ ] Configure `pom.xml` with dependencies (Spring Web, Data JPA, Security, Validation, Flyway/Liquibase, PostgreSQL, Testcontainers, SpringDoc OpenAPI)
- [ ] Create `docker-compose.yml` for PostgreSQL
- [ ] Configure `application.yml` with database and JWT settings
- [ ] Setup Flyway/Liquibase directory structure

## Phase 2: Database Migrations
- [ ] Create initial migration: users table
- [ ] Create migration: products table
- [ ] Create migration: orders and order_items tables
- [ ] Create seed migration: 10+ GPU products

## Phase 3: Core Domain Implementation
- [ ] Create base entity with common fields
- [ ] Implement User entity, repository, service, controller
- [ ] Implement Product entity, repository, service, controller
- [ ] Implement Order/OrderItem entities, repositories, services, controller
- [ ] Implement DTOs as Java Records

## Phase 4: Security Implementation
- [ ] Configure Spring Security with JWT
- [ ] Implement JWT token generation and validation
- [ ] Create Authentication controller
- [ ] Secure endpoints with proper authorization

## Phase 5: API Standards
- [ ] Implement global exception handler
- [ ] Configure standard error responses (400, 401, 404, 500)
- [ ] Add input validation with Bean Validation
- [ ] Configure OpenAPI/Swagger documentation

## Phase 6: Testing
- [ ] Configure Testcontainers with singleton pattern
- [ ] Write integration test: User registration
- [ ] Write integration test: Authentication flow
- [ ] Write integration test: Product CRUD
- [ ] Write integration test: Order creation with stock validation
- [ ] Write integration test: Unauthorized access rejection

## Phase 7: Documentation
- [ ] Update README.md with:
  - Project overview
  - Prerequisites
  - How to run Docker Compose for PostgreSQL
  - How to run database migrations
  - How to start the application
  - API documentation access (Swagger UI URL)

---

# HTTP Error States

| Code | Status | Trigger |
|------|--------|---------|
| 400 | Bad Request | Validation failure (DTO constraints) |
| 401 | Unauthorized | Invalid/Missing JWT Token |
| 404 | Not Found | Resource does not exist |
| 500 | Internal Server Error | Unexpected server failure |
