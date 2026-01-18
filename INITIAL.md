# System Context

## Global Goal
Refactor the synchronous E-commerce Backend into an **Event-Driven Architecture** to handle high-load order processing asynchronously. Introduce background job scheduling for order expiration and a notification system for audit trails.

## Scope
1.  **Infrastructure:** Add RabbitMQ (Docker) and Event Bus abstraction.
2.  **Async Processing:** Decouple Order placement from processing (Simulation of 5s payment).
3.  **Scheduler:** Automated cleanup of stale orders (CRON).
4.  **Notifications:** Audit log for system events.
5.  **Frontend Update:** React UI must poll for status changes.

---

# Technology Standards

## Core Stack (additions)
| Component | Technology | Version/Constraint |
|-----------|------------|-------------------|
| Messaging | **RabbitMQ** | 3.12-management (Docker) |
| Framework | **Spring AMQP** | Spring Boot Starter AMQP |
| Scheduling | **Spring Scheduler** | `@EnableScheduling` |
| JSON | Jackson | JavaTimeModule (for LocalDateTime in Events) |
| Logging | SLF4J / Logback | **MANDATORY:** Log every event publish/consume |
| Documentation| Javadoc | **MANDATORY:** Class/Method level docs for all new Async components |

---

# Active Iteration Scope

## Feature: Event-Driven Order Lifecycle & Notifications

## Backend Tasks (Global Part 5)
1.  **Infra:** Update `docker-compose.yml` with RabbitMQ.
2.  **Core:** Implement `EventBus` interface and `RabbitMqEventBus`.
3.  **Domain Events:** Create Records: `OrderCreatedEvent`, `OrderCompletedEvent`, `OrderExpiredEvent`.
4.  **Order Service Refactor:**
    * Change `createOrder` to ONLY save `PENDING` status and publish `OrderCreatedEvent`.
    * Remove synchronous stock deduction (or move to listener).
5.  **Async Processor (`OrderProcessor`):**
    * Listen to `OrderCreatedEvent`.
    * Update status to `PROCESSING`.
    * `Thread.sleep(5000)` (Simulate Payment).
    * **Logic:** 50% chance -> `COMPLETED` + Publish `OrderCompletedEvent`.
    * **Logic:** 50% chance -> Do nothing (Stay `PROCESSING`).
6.  **Scheduler (`OrderExpirationJob`):**
    * Run every 60s.
    * Query: `status = PROCESSING` AND `updatedAt < now - 10min`.
    * Action: Update to `EXPIRED` + Publish `OrderExpiredEvent`.
7.  **Notification Service:**
    * Listen to `OrderCompleted` and `OrderExpired`.
    * Log "Fake Email" to console.
    * Save to new `notifications` table.

## Frontend Tasks (Global Part 6)
1.  **Polling Hook:** Create `useOrderPolling(orderId)` hook.
2.  **Order Detail UI:**
    * Refactor `OrdersPage.jsx` / `OrderCard.jsx`.
    * If status is `PENDING` or `PROCESSING`, auto-refresh every 3s.
    * Show "Processing Payment..." spinner/toast.
    * Stop polling when `COMPLETED` or `EXPIRED`.

---

# Functional Specifications

## User Stories
* **US-Async-1:** As a system, when an order is created, I process it in the background so the user API response is instant.
* **US-Async-2:** As a system, I automatically expire orders that are stuck in processing for more than 10 minutes.
* **US-Async-3:** As an admin, I want to see a notification/audit log in the database whenever an order is completed or expired.

## Validation Rules
* **State Machine:**
    * `PENDING` -> `PROCESSING` (via Processor)
    * `PROCESSING` -> `COMPLETED` (via Processor 50%)
    * `PROCESSING` -> `EXPIRED` (via Scheduler)
* **Data Integrity:** Notification record must link to `order_id`.

---

# Architecture & Data Model

## Database Schema Changes

### New Table: `notifications`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO |
| order_id | BIGINT | FK -> orders(id) |
| type | VARCHAR(50) | 'EMAIL', 'SYSTEM_ALERT' |
| message | TEXT | Content of the event |
| sent_at | TIMESTAMP | DEFAULT NOW |

## Domain Events (JSON Payloads)

**OrderCreatedEvent**
```json
{ "orderId": 123, "userId": 45, "total": 1599.99, "timestamp": "2026-01-15T10:00:00" }