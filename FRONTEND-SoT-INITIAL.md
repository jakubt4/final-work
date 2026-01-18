# System Context
**Goal:** A high-performance E-commerce platform for selling GPUs.
**Current State:** Backend is fully implemented (Spring Boot 3.4), secured (JWT), and tested.
**Objective:** Create a lightweight "Disposable UI" to visually test the User Journeys (Auth -> Browse -> Order).

# Technology Standards
**Backend (Existing):**
- Java 21, Spring Boot 3.4.1
- PostgreSQL 16, Flyway
- JWT Auth (Bearer)
- Port: 8080

**Frontend (New - To Be Implemented):**
- **Framework:** React 18 + Vite (Minimal setup)
- **Styling:** Tailwind CSS (via CDN or simple setup to save usage)
- **Http Client:** Axios (configured with Base URL)
- **Routing:** React Router DOM

# Active Iteration Scope
**Feature:** Minimalist React Frontend (The "UI Client")
**Tasks:**
1.  **Setup:** Initialize Vite project with Proxy to `http://localhost:8080`.
2.  **Auth Module:**
    - Login Screen (Email/Password) -> Stores JWT in localStorage.
    - Axios Interceptor -> Auto-attaches `Authorization: Bearer <token>`.
3.  **Product Module:**
    - Fetch GET `/api/products` (Public/Protected check).
    - Render grid of 12 GPUs.
4.  **Order Module:**
    - "Buy" button on product -> Calls POST `/api/orders`.
    - Simple "My Orders" list fetching GET `/api/orders`.

# Architecture & Data Model (JSON Contracts)

**User (Auth):**
- Login Request: `{ "email": "...", "password": "..." }`
- Token Response: `{ "token": "...", "type": "Bearer", "expiresIn": 86400 }`

**Product:**
- JSON: `{ "id": 1, "name": "RTX 4090", "price": 1599.99, "stock": 15 }`

**Order:**
- Create Request: `{ "items": [ { "productId": 1, "quantity": 1 } ] }`

# Implementation Roadmap
- [x] Part 1: Infra & Database (Postgres/Flyway)
- [x] Part 2: Security & User Auth (JWT)
- [x] Part 3: Product & Order Logic (Pessimistic Locking)
- [x] Part 4: Integration Tests & CI
- [ ] **Part 5: React UI Client (Current Focus)**