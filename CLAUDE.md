# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

**Infrastructure (required first):**
```bash
docker-compose up -d            # Starts PostgreSQL and RabbitMQ
```

**Backend (Spring Boot):**
```bash
./mvnw spring-boot:run          # Linux/Mac
mvnw.cmd spring-boot:run        # Windows
```

**Frontend (React):**
```bash
cd frontend
npm install
npm run dev                     # Dev server at http://localhost:5173
npm run build                   # Production build
npm run lint                    # ESLint check
```

**Testing:**
```bash
./mvnw clean verify             # All tests (requires Docker for Testcontainers)
./mvnw test                     # Unit tests only
./mvnw test -Dtest=OrderControllerIT  # Run single test class
```

**API Docs:** http://localhost:8080/swagger-ui.html
**RabbitMQ Management:** http://localhost:15672 (guest/guest)

## Architecture Overview

This is a GPU e-commerce platform with a Spring Boot 3.4.1 backend (Java 21) and React 19 frontend, using event-driven architecture for async order processing.

### Backend Structure (`src/main/java/com/gpustore/`)

| Module | Purpose |
|--------|---------|
| `auth/` | Login endpoint, JWT token generation |
| `security/` | JwtTokenProvider, JwtAuthenticationFilter, UserPrincipal |
| `user/` | User entity, CRUD operations |
| `product/` | Product entity, catalog management |
| `order/` | Order + OrderItem entities, OrderProcessor (async), OrderExpirationJob (scheduler) |
| `event/` | EventBus interface, RabbitMqEventBus, domain events (OrderCreated/Completed/Expired) |
| `notification/` | Notification entity and service, consumes order events |
| `config/` | SecurityConfig, RabbitMqConfig (exchange/queues), OpenApiConfig |
| `common/` | BaseEntity (audit timestamps), GlobalExceptionHandler |

**Pattern:** Controllers → Services → Repositories with DTOs for request/response.

### Frontend Structure (`frontend/src/`)

| Directory | Purpose |
|-----------|---------|
| `api/axios.js` | Axios client with JWT interceptor (auto-attaches Bearer token) |
| `context/AuthContext.jsx` | Auth state, login/logout functions |
| `hooks/` | useProducts, useOrders for data fetching |
| `pages/` | LoginPage, ProductsPage, OrdersPage |
| `components/` | ProtectedRoute, Layout, Navbar, UI components |

### Event-Driven Order Flow

```
POST /api/orders → OrderService creates PENDING order
                 → Publishes OrderCreatedEvent to RabbitMQ
                 ↓
OrderProcessor consumes event → Sets status to PROCESSING
                              → Simulates payment (5s delay, 50% success)
                              → On success: deducts stock, sets COMPLETED, publishes OrderCompletedEvent
                              → On failure: stays PROCESSING
                              ↓
OrderExpirationJob (runs every 60s) → Finds PROCESSING orders older than 10 min
                                    → Sets status to EXPIRED, publishes OrderExpiredEvent
                              ↓
NotificationService consumes Completed/Expired events → Creates notification records
```

**Order statuses:** PENDING → PROCESSING → COMPLETED or EXPIRED

**RabbitMQ Topology:**
- Exchange: `orders.exchange` (direct)
- Queues: `orders.created.queue`, `orders.completed.queue`, `orders.expired.queue`, `orders.dlq`

### Database

- PostgreSQL 16 + RabbitMQ 3.12 via Docker
- Flyway migrations in `src/main/resources/db/migration/`
- Tables: `users`, `products`, `orders`, `order_items`, `notifications`
- 12 seeded GPU products (NVIDIA, AMD, Intel)

### Authentication Flow

1. POST `/api/auth/login` with email/password
2. Backend returns JWT token (24h expiry)
3. Frontend stores in localStorage
4. Axios interceptor attaches `Authorization: Bearer <token>` to all requests
5. On 401, interceptor clears token and redirects to login

### Key Relationships

- Order has many OrderItems (cascade delete)
- OrderItem references Product (lazy loaded - use `findByIdWithItems` queries to avoid LazyInitializationException)
- Order belongs to User
- Notification belongs to User

## Testing

Integration tests use Testcontainers with real PostgreSQL and RabbitMQ. Test base class: `AbstractIntegrationTest` provides auth helpers and cleanup.

Test files: `AuthControllerIT`, `UserControllerIT`, `ProductControllerIT`, `OrderControllerIT`, `OrderAsyncFlowIT`, `OrderExpirationJobIT`, `SecurityIT`
