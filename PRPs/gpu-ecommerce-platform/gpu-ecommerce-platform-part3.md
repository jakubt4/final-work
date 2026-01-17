# PRP Part 3: Product & Order Modules (To Be Expanded)

<!-- STATUS: OUTLINE ONLY - Run /execute-prp to expand with full implementation details -->

**Feature:** GPU E-commerce Platform - Product Catalog & Order Processing
**Estimated Files:** 14 files
**Dependencies:** Part 1 and Part 2 must be completed first

---

## Modules Covered

### 1. Product Module

- `src/main/java/com/gpustore/product/Product.java`
  - Entity extending BaseEntity
  - Fields: name, description, price (BigDecimal), stock (Integer)
  - Validation constraints in entity

- `src/main/java/com/gpustore/product/ProductRepository.java`
  - JpaRepository<Product, Long>
  - findByIdWithLock() with @Lock(PESSIMISTIC_WRITE)

- `src/main/java/com/gpustore/product/ProductService.java`
  - CRUD operations
  - Stock validation (>= 0)
  - Price validation (>= 0)

- `src/main/java/com/gpustore/product/ProductController.java`
  - POST /api/products (protected)
  - GET /api/products (protected)
  - GET /api/products/{id} (protected)
  - PUT /api/products/{id} (protected)
  - DELETE /api/products/{id} (protected)

- `src/main/java/com/gpustore/product/dto/CreateProductRequest.java`
  - Record with validation annotations

- `src/main/java/com/gpustore/product/dto/UpdateProductRequest.java`
  - Record for partial updates

- `src/main/java/com/gpustore/product/dto/ProductResponse.java`
  - Record with all product fields

---

### 2. Order Module

- `src/main/java/com/gpustore/order/Order.java`
  - Entity extending BaseEntity
  - ManyToOne relationship with User
  - OneToMany relationship with OrderItem (cascade ALL)
  - Fields: user, total, status, items

- `src/main/java/com/gpustore/order/OrderItem.java`
  - Entity (not extending BaseEntity - no auditing needed)
  - ManyToOne with Order
  - ManyToOne with Product
  - Fields: order, product, quantity, price (captured at order time)

- `src/main/java/com/gpustore/order/OrderStatus.java`
  - Enum: PENDING, PROCESSING, COMPLETED, EXPIRED

- `src/main/java/com/gpustore/order/OrderRepository.java`
  - JpaRepository<Order, Long>
  - findByUserId(Long userId)

- `src/main/java/com/gpustore/order/OrderService.java`
  - Create order with pessimistic locking
  - Calculate total from items
  - Reduce product stock atomically
  - Validate stock availability
  - Status update with lifecycle validation

- `src/main/java/com/gpustore/order/OrderController.java`
  - POST /api/orders (protected)
  - GET /api/orders (protected)
  - GET /api/orders/{id} (protected)
  - PUT /api/orders/{id} (protected)
  - DELETE /api/orders/{id} (protected)

- `src/main/java/com/gpustore/order/dto/CreateOrderRequest.java`
  - Record with List<OrderItemRequest>

- `src/main/java/com/gpustore/order/dto/OrderItemRequest.java`
  - Record: productId, quantity

- `src/main/java/com/gpustore/order/dto/UpdateOrderRequest.java`
  - Record for status updates

- `src/main/java/com/gpustore/order/dto/OrderResponse.java`
  - Record with nested OrderItemResponse list

- `src/main/java/com/gpustore/order/dto/OrderItemResponse.java`
  - Record: id, productId, productName, quantity, price

---

## High-Level Requirements

### Product Validation Rules
- name: @NotBlank, @Size(max=100)
- description: Optional (nullable)
- price: @NotNull, @DecimalMin("0.00")
- stock: @NotNull, @Min(0)

### Order Validation Rules
- items: @NotEmpty (at least one item required)
- Each item.productId: @NotNull, must exist
- Each item.quantity: @NotNull, @Min(1)

### Order Creation Logic (Pseudocode)
```
@Transactional
createOrder(userId, items):
  1. Validate user exists
  2. For each item:
     a. Lock product row (PESSIMISTIC_WRITE)
     b. Check stock >= quantity
     c. If insufficient: throw ValidationException
     d. Reduce stock by quantity
     e. Capture current price
  3. Calculate total = sum(quantity * price)
  4. Create Order with status = PENDING
  5. Save and return
```

### Order Status Lifecycle
- Valid transitions:
  - PENDING → PROCESSING
  - PROCESSING → COMPLETED
  - PENDING → EXPIRED
  - PROCESSING → EXPIRED
- Invalid transitions should throw ValidationException

### Concurrency Control
- Use @Lock(LockModeType.PESSIMISTIC_WRITE) on product lookup
- Ensures no overselling under concurrent requests
- Transaction isolation handles race conditions

---

## Validation Gates

```bash
# After implementation:
./mvnw clean compile

# Get auth token first
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}' | jq -r '.token')

# Test product listing:
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"

# Test order creation:
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":2}]}'

# Verify stock reduced:
curl http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer $TOKEN"
```
