# ENHANCED-INITIAL: Event-Driven Order Lifecycle & Notifications

> **Enhanced Specification with Codebase Context**
> Original: `INITIAL.md` | Enhanced: 2026-01-18

---

## System Context

### Global Goal
Refactor the synchronous E-commerce Backend into an **Event-Driven Architecture** to handle high-load order processing asynchronously. Introduce background job scheduling for order expiration and a notification system for audit trails.

### Scope
1. **Infrastructure:** Add RabbitMQ (Docker) and Event Bus abstraction.
2. **Async Processing:** Decouple Order placement from processing (Simulation of 5s payment).
3. **Scheduler:** Automated cleanup of stale orders (CRON).
4. **Notifications:** Audit log for system events.
5. **Frontend Update:** React UI must poll for status changes.

---

## Definition of Done (This Iteration)

| Criterion | Acceptance |
|-----------|------------|
| RabbitMQ running | `docker-compose up -d` starts RabbitMQ alongside PostgreSQL |
| Order creation instant | POST `/api/orders` returns `PENDING` in <100ms (no blocking) |
| Async processing | Order transitions to `PROCESSING` within 1s of creation |
| Payment simulation | 5s delay with 50% success rate |
| Expiration job | Orders stuck in `PROCESSING` >10min are marked `EXPIRED` |
| Notifications persisted | All `COMPLETED`/`EXPIRED` events logged to `notifications` table |
| Frontend polling | UI auto-refreshes every 3s for active orders |
| All existing tests pass | `./mvnw clean verify` green |
| New integration tests | Cover async flow, expiration, notifications |

---

## Architectural Compliance Check

### ✅ Modular Monolith Alignment
- New components follow existing package structure: `com.gpustore.<module>`
- Event-driven pattern does not introduce microservices—stays within single deployable unit
- Uses Spring AMQP (same process, RabbitMQ for durability)

### ✅ Java 21 Standards
- Domain events as **Records** (immutable, compact syntax)
- Pattern matching where applicable
- Virtual threads NOT required (Spring AMQP uses its own thread pool)

### ⚠️ Considerations
- **Transaction boundaries:** Event publishing must happen AFTER transaction commit
- **Idempotency:** Message consumers must handle duplicate deliveries
- **Testability:** Integration tests require RabbitMQ (use Testcontainers)

---

## Gap Analysis

### Missing Data
| Gap | Resolution |
|-----|------------|
| No `notifications` table | Add Flyway migration `V3__create_notifications_table.sql` |
| No event publishing infrastructure | Create `event/` package with `EventBus` interface |
| No RabbitMQ config | Add `RabbitMqConfig.java` for queues/exchanges |

### Edge Cases to Handle
| Scenario | Handling |
|----------|----------|
| RabbitMQ unavailable at startup | Fail fast with clear error message |
| Message processing fails | Dead-letter queue (DLQ) for retry/investigation |
| Multiple consumers process same order | Optimistic locking on status update |
| Order deleted while processing | Check existence before status update |
| Scheduler runs during high load | Batch processing with pagination |

### Security Implications
| Concern | Mitigation |
|---------|------------|
| Notification data exposure | `notifications` table contains no PII—only order IDs |
| Message tampering | Internal RabbitMQ (localhost only)—no external exposure |
| AuthZ for notifications API | Admin-only endpoint if exposed (consider future) |

---

## Technology Standards

### Core Stack Additions
| Component | Technology | Version/Constraint |
|-----------|------------|-------------------|
| Messaging | **RabbitMQ** | 3.12-management (Docker) |
| Framework | **Spring AMQP** | Spring Boot Starter AMQP |
| Scheduling | **Spring Scheduler** | `@EnableScheduling` |
| JSON | Jackson | JavaTimeModule (for LocalDateTime in Events) |
| Logging | SLF4J / Logback | **MANDATORY:** Log every event publish/consume |
| Documentation | Javadoc | **MANDATORY:** Class/Method level docs for all new Async components |

### Maven Dependencies to Add
```xml
<!-- pom.xml additions -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>rabbitmq</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Codebase Contextualization

### Existing Files to Modify

| File | Modification |
|------|--------------|
| `docker-compose.yml` | Add RabbitMQ service |
| `src/main/java/com/gpustore/order/OrderService.java` | Remove synchronous stock processing, publish `OrderCreatedEvent` |
| `src/main/java/com/gpustore/order/OrderStatus.java` | Already has all required states: `PENDING`, `PROCESSING`, `COMPLETED`, `EXPIRED` |
| `src/main/java/com/gpustore/GpuStoreApplication.java` | Add `@EnableScheduling` |
| `frontend/src/pages/OrdersPage.jsx` | Add polling for active orders |
| `frontend/src/components/OrderCard.jsx` | Add processing spinner/indicator |
| `frontend/src/hooks/useOrders.js` | Add order polling hook |

### New Files to Create

#### Backend - Event Infrastructure
| File Path | Purpose |
|-----------|---------|
| `src/main/java/com/gpustore/event/EventBus.java` | Interface for event publishing |
| `src/main/java/com/gpustore/event/RabbitMqEventBus.java` | RabbitMQ implementation |
| `src/main/java/com/gpustore/event/OrderCreatedEvent.java` | Domain event record |
| `src/main/java/com/gpustore/event/OrderCompletedEvent.java` | Domain event record |
| `src/main/java/com/gpustore/event/OrderExpiredEvent.java` | Domain event record |
| `src/main/java/com/gpustore/config/RabbitMqConfig.java` | Queues, exchanges, bindings |

#### Backend - Async Processing
| File Path | Purpose |
|-----------|---------|
| `src/main/java/com/gpustore/order/OrderProcessor.java` | Listens to `OrderCreatedEvent`, simulates payment |
| `src/main/java/com/gpustore/order/OrderExpirationJob.java` | Scheduled job for expiring stale orders |
| `src/main/java/com/gpustore/order/OrderRepository.java` | Add query: `findByStatusAndUpdatedAtBefore()` |

#### Backend - Notifications
| File Path | Purpose |
|-----------|---------|
| `src/main/java/com/gpustore/notification/Notification.java` | Entity extending `BaseEntity` |
| `src/main/java/com/gpustore/notification/NotificationType.java` | Enum: `EMAIL`, `SYSTEM_ALERT` |
| `src/main/java/com/gpustore/notification/NotificationRepository.java` | JPA Repository |
| `src/main/java/com/gpustore/notification/NotificationService.java` | Listens to events, persists notifications |
| `src/main/resources/db/migration/V3__create_notifications_table.sql` | Flyway migration |

#### Frontend
| File Path | Purpose |
|-----------|---------|
| `frontend/src/hooks/useOrderPolling.js` | Polling hook for single order status |

### Reusable Patterns from Codebase

#### Follow: BaseEntity Pattern
```java
// Location: src/main/java/com/gpustore/common/BaseEntity.java
// Notification entity should extend BaseEntity for:
// - Auto-generated ID
// - createdAt (audit timestamp)
// - updatedAt (audit timestamp)
```

#### Follow: OrderService Transaction Pattern
```java
// Location: src/main/java/com/gpustore/order/OrderService.java
// Pattern: @Transactional at method level
// Pattern: Logger at class level (SLF4J)
@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Transactional
    public Order createOrder(CreateOrderRequest request, Long userId) {
        // Business logic here
    }
}
```

#### Follow: Repository Query Pattern
```java
// Location: src/main/java/com/gpustore/order/OrderRepository.java
// Pattern: Custom @Query for complex lookups with eager loading
@Query("SELECT o FROM Order o WHERE o.status = :status AND o.updatedAt < :cutoff")
List<Order> findByStatusAndUpdatedAtBefore(@Param("status") OrderStatus status,
                                            @Param("cutoff") LocalDateTime cutoff);
```

#### Follow: DTO Record Pattern
```java
// Location: src/main/java/com/gpustore/order/OrderResponse.java
// Pattern: Records for immutable DTOs
public record OrderResponse(
    Long id,
    Long userId,
    BigDecimal total,
    String status,
    List<OrderItemResponse> items,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

#### Follow: Frontend Hook Pattern
```javascript
// Location: frontend/src/hooks/useOrders.js
// Pattern: State for data, loading, error + refetch function
const [orders, setOrders] = useState([]);
const [loading, setLoading] = useState(true);
const [error, setError] = useState(null);

const fetchOrders = useCallback(async () => {
  try {
    setLoading(true);
    const response = await api.get('/orders');
    setOrders(response.data);
  } catch (err) {
    setError(err.message);
  } finally {
    setLoading(false);
  }
}, []);
```

---

## Functional Specifications

### User Stories
| ID | Story | Acceptance Criteria |
|----|-------|---------------------|
| US-Async-1 | As a system, when an order is created, I process it in the background so the user API response is instant. | Order created with `PENDING` status, response <100ms, async processing starts |
| US-Async-2 | As a system, I automatically expire orders that are stuck in processing for more than 10 minutes. | Scheduler runs every 60s, updates status to `EXPIRED`, publishes `OrderExpiredEvent` |
| US-Async-3 | As an admin, I want to see a notification/audit log in the database whenever an order is completed or expired. | `notifications` table populated with order_id, type, message, timestamp |
| US-Frontend-1 | As a user, I see real-time status updates for my orders without refreshing the page. | Polling every 3s for `PENDING`/`PROCESSING` orders, stops on terminal states |

### State Machine

```
                    ┌─────────────┐
                    │   PENDING   │
                    └──────┬──────┘
                           │ OrderProcessor consumes event
                           ▼
                    ┌─────────────┐
           ┌────────│ PROCESSING  │────────┐
           │        └─────────────┘        │
           │ 50% success                   │ Scheduler (>10min)
           ▼                               ▼
    ┌─────────────┐                 ┌─────────────┐
    │  COMPLETED  │                 │   EXPIRED   │
    └─────────────┘                 └─────────────┘
    (terminal state)                (terminal state)
```

### Validation Rules
| Rule | Implementation |
|------|----------------|
| Valid state transitions only | Validate in `OrderProcessor` and `OrderExpirationJob` before update |
| Stock deduction timing | Move to `OrderProcessor` on successful payment (COMPLETED transition) |
| Notification links to order | FK constraint `order_id` → `orders(id)` |
| Idempotent processing | Check current status before transition to prevent race conditions |

---

## Architecture & Data Model

### Database Schema Changes

#### New Migration: `V3__create_notifications_table.sql`
```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL CHECK (type IN ('EMAIL', 'SYSTEM_ALERT')),
    message TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_order_id ON notifications(order_id);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_sent_at ON notifications(sent_at);
```

#### New Index for Expiration Query
```sql
-- Add to V3 or create V4
CREATE INDEX idx_orders_status_updated_at ON orders(status, updated_at);
```

### Domain Events (JSON Payloads)

#### OrderCreatedEvent
```java
// File: src/main/java/com/gpustore/event/OrderCreatedEvent.java
public record OrderCreatedEvent(
    Long orderId,
    Long userId,
    BigDecimal total,
    LocalDateTime timestamp
) {}
```

**Example JSON:**
```json
{
  "orderId": 123,
  "userId": 45,
  "total": 1599.99,
  "timestamp": "2026-01-15T10:00:00"
}
```

#### OrderCompletedEvent
```java
// File: src/main/java/com/gpustore/event/OrderCompletedEvent.java
public record OrderCompletedEvent(
    Long orderId,
    Long userId,
    BigDecimal total,
    LocalDateTime timestamp
) {}
```

#### OrderExpiredEvent
```java
// File: src/main/java/com/gpustore/event/OrderExpiredEvent.java
public record OrderExpiredEvent(
    Long orderId,
    Long userId,
    String reason,
    LocalDateTime timestamp
) {}
```

### RabbitMQ Configuration

#### Exchanges & Queues
| Exchange | Queue | Routing Key | Consumer |
|----------|-------|-------------|----------|
| `orders.exchange` (direct) | `orders.created.queue` | `order.created` | `OrderProcessor` |
| `orders.exchange` (direct) | `orders.completed.queue` | `order.completed` | `NotificationService` |
| `orders.exchange` (direct) | `orders.expired.queue` | `order.expired` | `NotificationService` |
| | `orders.dlq` | (dead letters) | Manual investigation |

#### application.yml Additions
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    listener:
      simple:
        acknowledge-mode: auto
        retry:
          enabled: true
          initial-interval: 1000
          max-attempts: 3
```

### Docker Compose Update

```yaml
# docker-compose.yml additions
services:
  postgres:
    # ... existing config ...

  rabbitmq:
    image: rabbitmq:3.12-management
    container_name: gpustore-rabbitmq
    ports:
      - "5672:5672"   # AMQP
      - "15672:15672" # Management UI
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
  rabbitmq_data:
```

---

## Backend Implementation Details

### 1. EventBus Interface
```java
// src/main/java/com/gpustore/event/EventBus.java
/**
 * Abstraction for event publishing.
 * Allows switching between RabbitMQ and other implementations.
 */
public interface EventBus {
    void publish(String routingKey, Object event);
}
```

### 2. RabbitMqEventBus Implementation
```java
// src/main/java/com/gpustore/event/RabbitMqEventBus.java
@Service
@RequiredArgsConstructor
public class RabbitMqEventBus implements EventBus {
    private static final Logger log = LoggerFactory.getLogger(RabbitMqEventBus.class);
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(String routingKey, Object event) {
        log.info("Publishing event [{}]: {}", routingKey, event);
        rabbitTemplate.convertAndSend("orders.exchange", routingKey, event);
    }
}
```

### 3. OrderService Refactor

**Current (synchronous):**
```java
// Stock deducted immediately during order creation
product.setStock(product.getStock() - quantity);
productRepository.save(product);
```

**Target (asynchronous):**
```java
// OrderService.createOrder() changes:
// 1. Save order with PENDING status (NO stock deduction)
// 2. Publish OrderCreatedEvent AFTER transaction commits

@Transactional
public Order createOrder(CreateOrderRequest request, Long userId) {
    // ... create order with PENDING status ...
    Order savedOrder = orderRepository.save(order);

    // Publish event after successful save
    eventBus.publish("order.created", new OrderCreatedEvent(
        savedOrder.getId(),
        userId,
        savedOrder.getTotal(),
        LocalDateTime.now()
    ));

    return savedOrder;
}
```

### 4. OrderProcessor (Async Consumer)
```java
// src/main/java/com/gpustore/order/OrderProcessor.java
@Service
@RequiredArgsConstructor
public class OrderProcessor {
    private static final Logger log = LoggerFactory.getLogger(OrderProcessor.class);
    private static final Random RANDOM = new Random();

    @RabbitListener(queues = "orders.created.queue")
    @Transactional
    public void processOrder(OrderCreatedEvent event) {
        log.info("Processing order: {}", event.orderId());

        // 1. Update to PROCESSING
        Order order = orderRepository.findById(event.orderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order {} not in PENDING state, skipping", event.orderId());
            return;
        }

        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        // 2. Simulate payment (5 seconds)
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 3. 50% success rate
        if (RANDOM.nextBoolean()) {
            // Deduct stock NOW (on success)
            deductStock(order);
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);

            eventBus.publish("order.completed", new OrderCompletedEvent(
                order.getId(),
                order.getUser().getId(),
                order.getTotal(),
                LocalDateTime.now()
            ));
            log.info("Order {} completed successfully", order.getId());
        } else {
            // Stay in PROCESSING (will be expired by scheduler)
            log.info("Order {} payment failed, will be expired by scheduler", order.getId());
        }
    }
}
```

### 5. OrderExpirationJob (Scheduler)
```java
// src/main/java/com/gpustore/order/OrderExpirationJob.java
@Component
@RequiredArgsConstructor
public class OrderExpirationJob {
    private static final Logger log = LoggerFactory.getLogger(OrderExpirationJob.class);

    @Scheduled(fixedRate = 60000) // Every 60 seconds
    @Transactional
    public void expireStaleOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);

        List<Order> staleOrders = orderRepository
            .findByStatusAndUpdatedAtBefore(OrderStatus.PROCESSING, cutoff);

        log.info("Found {} stale orders to expire", staleOrders.size());

        for (Order order : staleOrders) {
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);

            eventBus.publish("order.expired", new OrderExpiredEvent(
                order.getId(),
                order.getUser().getId(),
                "Processing timeout exceeded 10 minutes",
                LocalDateTime.now()
            ));

            log.info("Order {} expired", order.getId());
        }
    }
}
```

### 6. NotificationService (Event Listener)
```java
// src/main/java/com/gpustore/notification/NotificationService.java
@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @RabbitListener(queues = "orders.completed.queue")
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("📧 Fake Email: Order {} completed for user {}",
            event.orderId(), event.userId());

        saveNotification(event.orderId(), NotificationType.EMAIL,
            String.format("Order #%d completed. Total: $%.2f",
                event.orderId(), event.total()));
    }

    @RabbitListener(queues = "orders.expired.queue")
    public void handleOrderExpired(OrderExpiredEvent event) {
        log.info("⚠️ System Alert: Order {} expired - {}",
            event.orderId(), event.reason());

        saveNotification(event.orderId(), NotificationType.SYSTEM_ALERT,
            String.format("Order #%d expired: %s",
                event.orderId(), event.reason()));
    }
}
```

---

## Frontend Implementation Details

### 1. useOrderPolling Hook
```javascript
// frontend/src/hooks/useOrderPolling.js
import { useState, useEffect, useCallback } from 'react';
import api from '../api/axios';

const ACTIVE_STATUSES = ['PENDING', 'PROCESSING'];
const POLL_INTERVAL = 3000; // 3 seconds

export function useOrderPolling(orderId, initialStatus) {
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isPolling, setIsPolling] = useState(
    ACTIVE_STATUSES.includes(initialStatus)
  );

  const fetchOrder = useCallback(async () => {
    try {
      const response = await api.get(`/orders/${orderId}`);
      setOrder(response.data);

      // Stop polling on terminal states
      if (!ACTIVE_STATUSES.includes(response.data.status)) {
        setIsPolling(false);
      }
    } catch (err) {
      setError(err.message);
      setIsPolling(false);
    }
  }, [orderId]);

  useEffect(() => {
    if (!isPolling) return;

    const intervalId = setInterval(fetchOrder, POLL_INTERVAL);
    return () => clearInterval(intervalId);
  }, [isPolling, fetchOrder]);

  return { order, loading, error, isPolling };
}
```

### 2. OrderCard Updates
```javascript
// frontend/src/components/OrderCard.jsx additions
import { useOrderPolling } from '../hooks/useOrderPolling';

// Inside OrderCard component:
const { order: polledOrder, isPolling } = useOrderPolling(order.id, order.status);
const displayOrder = polledOrder || order;

// Add spinner for active states:
{isPolling && (
  <div className="flex items-center gap-2 text-blue-600">
    <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
      {/* Spinner SVG */}
    </svg>
    <span>Processing payment...</span>
  </div>
)}
```

---

## Testing Strategy

### New Integration Tests Required

| Test Class | Coverage |
|------------|----------|
| `OrderAsyncFlowIT` | Full flow: create → process → complete/expire |
| `OrderExpirationJobIT` | Scheduler correctly expires stale orders |
| `NotificationServiceIT` | Notifications persisted for completed/expired orders |
| `EventBusIT` | Events published to correct queues |

### Testcontainers Setup
```java
// Add to AbstractIntegrationTest
@Container
static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.12-management");

@DynamicPropertySource
static void configureRabbitMQ(DynamicPropertyRegistry registry) {
    registry.add("spring.rabbitmq.host", rabbitmq::getHost);
    registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
}
```

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| RabbitMQ message loss | Orders stuck in PENDING forever | Configure message durability, add health monitoring |
| Race condition on status update | Duplicate processing | Check current status before update, use optimistic locking |
| Scheduler overlap | Same order expired multiple times | Use database-level locking or single-instance scheduler |
| Stock never deducted on failure | Inventory inconsistency | Stock deduction only on COMPLETED (current orders don't reserve stock) |
| Frontend over-polling | API overload | Stop polling on terminal states, use exponential backoff |

---

## Implementation Order (Recommended)

1. **Infrastructure** - Docker + RabbitMQ + Spring AMQP dependency
2. **Event abstractions** - EventBus interface, domain event records
3. **RabbitMQ configuration** - Queues, exchanges, bindings
4. **OrderService refactor** - Remove sync processing, publish events
5. **OrderProcessor** - Consume events, simulate payment
6. **Notifications module** - Entity, repository, service, migration
7. **Scheduler** - Expiration job
8. **Integration tests** - Full async flow coverage
9. **Frontend polling** - Hook + UI updates
10. **Manual E2E testing** - Full user journey verification
