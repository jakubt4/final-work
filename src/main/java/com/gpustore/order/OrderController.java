package com.gpustore.order;

import com.gpustore.order.dto.CreateOrderRequest;
import com.gpustore.order.dto.OrderResponse;
import com.gpustore.order.dto.UpdateOrderRequest;
import com.gpustore.security.UserPrincipal;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for order management operations.
 *
 * <p>Provides endpoints for CRUD operations on orders:</p>
 * <ul>
 *   <li>{@code POST /api/orders} - Create a new order</li>
 *   <li>{@code GET /api/orders} - List all orders</li>
 *   <li>{@code GET /api/orders/{id}} - Get order by ID</li>
 *   <li>{@code PUT /api/orders/{id}} - Update order status</li>
 *   <li>{@code DELETE /api/orders/{id}} - Delete order</li>
 * </ul>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    /**
     * Constructs a new OrderController with the required service.
     *
     * @param orderService the service for order operations
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Creates a new order for the authenticated user.
     *
     * @param principal the authenticated user principal
     * @param request   the order creation request with items
     * @return 201 Created with the created order details
     */
    @PostMapping
    public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                 @Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating new order for user: id={}", principal.getId());
        Order order = orderService.create(principal.getId(), request);
        log.info("Order created successfully: id={}, total={}", order.getId(), order.getTotal());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    /**
     * Retrieves all orders.
     *
     * @return 200 OK with a list of all orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll() {
        log.debug("Fetching all orders");
        List<OrderResponse> orders = orderService.findAll().stream()
                .map(OrderResponse::from)
                .toList();
        log.debug("Found {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param id the order ID
     * @return 200 OK with the order details, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        log.debug("Fetching order with id: {}", id);
        Order order = orderService.findById(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    /**
     * Updates an order's status.
     *
     * @param id      the order ID
     * @param request the update request with new status
     * @return 200 OK with the updated order details, or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateOrderRequest request) {
        log.info("Updating order status: id={}, newStatus={}", id, request.status());
        Order order = orderService.update(id, request);
        log.info("Order status updated successfully: id={}", id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    /**
     * Deletes an order by its ID.
     *
     * @param id the order ID
     * @return 204 No Content on success, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting order with id: {}", id);
        orderService.delete(id);
        log.info("Order deleted successfully: id={}", id);
        return ResponseEntity.noContent().build();
    }
}
