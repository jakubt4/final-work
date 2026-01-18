# GPU E-commerce Platform

A RESTful backend API for a GPU e-commerce platform built with Spring Boot.

## Overview

This platform provides a complete backend solution for an e-commerce store specializing in graphics processing units (GPUs). It includes user management, JWT-based authentication, product catalog management, and order processing with stock management.

## Technology Stack

### Backend
- **Java 21** - Latest LTS version with virtual threads support
- **Spring Boot 3.4.1** - Modern Spring framework
- **Spring Data JPA** - Database persistence
- **Spring Security** - Authentication and authorization
- **PostgreSQL 16** - Primary database
- **JWT (JJWT 0.12.6)** - Token-based authentication
- **Flyway** - Database migrations
- **Springdoc OpenAPI 2.7.0** - API documentation
- **Testcontainers** - Integration testing with real PostgreSQL

### Frontend
- **React 19** - Modern React with latest features
- **Vite 7** - Fast build tool and dev server
- **Tailwind CSS 4** - Utility-first CSS framework
- **React Router 7** - Client-side routing
- **Axios** - HTTP client for API communication

## Prerequisites

- Java 21 JDK
- Docker & Docker Compose
- Maven 3.9+ (or use the included Maven wrapper)
- Node.js 18+ and npm (for frontend)

## Quick Start

### Backend

1. **Start PostgreSQL:**
   ```bash
   docker-compose up -d
   ```

2. **Run the backend:**
   ```bash
   ./mvnw spring-boot:run
   ```
   On Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```

3. **Access Swagger UI:**
   Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) in your browser.

### Frontend

1. **Install dependencies:**
   ```bash
   cd frontend
   npm install
   ```

2. **Run the development server:**
   ```bash
   npm run dev
   ```

3. **Access the UI:**
   Open [http://localhost:5173](http://localhost:5173) in your browser.

### Available Frontend Scripts

| Script | Description |
|--------|-------------|
| `npm run dev` | Start development server with hot reload |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build locally |
| `npm run lint` | Run ESLint for code quality |

## API Documentation

Interactive API documentation is available at `/swagger-ui.html` when the application is running.

OpenAPI specification is available at `/api-docs`.

## Development Setup

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd gpu-ecommerce-platform
   ```

2. **Configure the database:**
   The default configuration expects PostgreSQL running on `localhost:5432` with database `gpustore`.

   Or use Docker Compose:
   ```bash
   docker-compose up -d
   ```

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

## Testing

Run all tests including integration tests:
```bash
./mvnw clean verify
```

Integration tests use Testcontainers to spin up a real PostgreSQL database, so Docker must be running.

Run only unit tests:
```bash
./mvnw test
```

## API Endpoints

### Authentication
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/login` | Login and receive JWT token | No |

### Users
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/users` | Register new user | No |
| GET | `/api/users` | List all users | Yes |
| GET | `/api/users/{id}` | Get user by ID | Yes |
| PUT | `/api/users/{id}` | Update user | Yes |
| DELETE | `/api/users/{id}` | Delete user | Yes |

### Products
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/products` | Create product | Yes |
| GET | `/api/products` | List all products | Yes |
| GET | `/api/products/{id}` | Get product by ID | Yes |
| PUT | `/api/products/{id}` | Update product | Yes |
| DELETE | `/api/products/{id}` | Delete product | Yes |

### Orders
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/orders` | Create order | Yes |
| GET | `/api/orders` | List all orders | Yes |
| GET | `/api/orders/{id}` | Get order by ID | Yes |
| PUT | `/api/orders/{id}` | Update order status | Yes |
| DELETE | `/api/orders/{id}` | Delete order | Yes |

## Authentication

The API uses JWT (JSON Web Token) for authentication.

1. **Register a user:**
   ```bash
   curl -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -d '{"name": "John Doe", "email": "john@example.com", "password": "password123"}'
   ```

2. **Login to get a token:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email": "john@example.com", "password": "password123"}'
   ```

3. **Use the token for protected endpoints:**
   ```bash
   curl -X GET http://localhost:8080/api/products \
     -H "Authorization: Bearer <your-token>"
   ```

## Project Structure

```
├── src/                        # Backend source
│   ├── main/
│   │   ├── java/com/gpustore/
│   │   │   ├── auth/           # Authentication module
│   │   │   ├── common/         # Shared utilities and exceptions
│   │   │   ├── config/         # Security and app configuration
│   │   │   ├── order/          # Order management module
│   │   │   ├── product/        # Product management module
│   │   │   ├── security/       # JWT and security components
│   │   │   └── user/           # User management module
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-test.yml
│   │       └── db/migration/   # Flyway migrations
│   └── test/
│       └── java/com/gpustore/  # Integration tests
│
└── frontend/                   # React frontend
    ├── src/
    │   ├── api/                # API client and service functions
    │   ├── components/         # Reusable UI components
    │   ├── context/            # React context providers
    │   ├── hooks/              # Custom React hooks
    │   ├── pages/              # Page components
    │   ├── App.jsx             # Main app component with routing
    │   └── main.jsx            # Application entry point
    ├── package.json
    └── vite.config.js
```

## Database

The application uses PostgreSQL with Flyway for database migrations. The seed data includes 12 GPU products from NVIDIA, AMD, and Intel.

### Tables
- `users` - User accounts
- `products` - GPU product catalog
- `orders` - Customer orders
- `order_items` - Order line items

## Configuration

Key configuration properties in `application.yml`:

| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | Server port | 8080 |
| `spring.datasource.url` | Database URL | jdbc:postgresql://localhost:5432/gpustore |
| `jwt.expiration` | Token expiration (ms) | 86400000 (24 hours) |

## License

This project is for educational purposes.
