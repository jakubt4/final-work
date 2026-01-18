# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

**Database (required first):**
```bash
docker-compose up -d
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
```

**API Docs:** http://localhost:8080/swagger-ui.html

## Architecture Overview

This is a GPU e-commerce platform with a Spring Boot 3.4.1 backend (Java 21) and React 19 frontend.

### Backend Structure (`src/main/java/com/gpustore/`)

| Module | Purpose |
|--------|---------|
| `auth/` | Login endpoint, JWT token generation |
| `security/` | JwtTokenProvider, JwtAuthenticationFilter, UserPrincipal |
| `user/` | User entity, CRUD operations |
| `product/` | Product entity, catalog management |
| `order/` | Order + OrderItem entities, order processing with stock management |
| `config/` | SecurityConfig (JWT filter chain, CORS), OpenApiConfig |
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

### Database

- PostgreSQL 16 via Docker
- Flyway migrations in `src/main/resources/db/migration/`
- Tables: `users`, `products`, `orders`, `order_items`
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

## Testing

Integration tests use Testcontainers with real PostgreSQL. Test base class: `AbstractIntegrationTest` provides auth helpers and cleanup.

Test files: `AuthControllerIT`, `UserControllerIT`, `ProductControllerIT`, `OrderControllerIT`, `SecurityIT`
