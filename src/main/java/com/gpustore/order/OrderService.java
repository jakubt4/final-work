package com.gpustore.order;

import com.gpustore.common.exception.ResourceNotFoundException;
import com.gpustore.common.exception.ValidationException;
import com.gpustore.config.RabbitMqConfig;
import com.gpustore.event.EventBus;
import com.gpustore.event.OrderCreatedEvent;
import com.gpustore.order.dto.CreateOrderRequest;
import com.gpustore.order.dto.OrderItemRequest;
import com.gpustore.order.dto.UpdateOrderRequest;
import com.gpustore.product.Product;
import com.gpustore.product.ProductRepository;
import com.gpustore.user.User;
import com.gpustore.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for order management operations.
 *
 * <p>Handles business logic for order processing including:</p>
 * <ul>
 *   <li>Order creation with async event publishing</li>
 *   <li>Order status transitions with validation</li>
 *   <li>Order retrieval and deletion</li>
 * </ul>
 *
 * <p>Note: Stock validation and deduction has been moved to async processing
 * via {@link OrderProcessor} to enable instant order creation response.</p>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EventBus eventBus;

    /**
     * Constructs a new OrderService with required dependencies.
     *
     * @param orderRepository   the repository for order persistence
     * @param productRepository the repository for product operations
     * @param userRepository    the repository for user lookups
     * @param eventBus          the event bus for publishing domain events
     */
    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        EventBus eventBus) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.eventBus = eventBus;
    }

    /**
     * Creates a new order for a user.
     *
     * <p>Creates the order with PENDING status and publishes an OrderCreatedEvent
     * for async processing. Stock validation and deduction is handled asynchronously
     * by the OrderProcessor.</p>
     *
     * @param userId  the ID of the user placing the order
     * @param request the order creation request with items
     * @return the created order entity with PENDING status
     * @throws ResourceNotFoundException if user or any product is not found
     */
    public Order create(Long userId, CreateOrderRequest request) {
        log.debug("Creating order for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Order order = new Order(user, BigDecimal.ZERO, OrderStatus.PENDING);
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            // Validate product exists (stock validation moved to async processing)
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemRequest.productId()));

            // Capture price at order time (stock deduction moved to OrderProcessor)
            BigDecimal itemPrice = product.getPrice();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));
            total = total.add(itemTotal);

            OrderItem orderItem = new OrderItem(order, product, itemRequest.quantity(), itemPrice);
            order.addItem(orderItem);
        }

        order.setTotal(total);
        Order savedOrder = orderRepository.save(order);
        log.info("Order created: id={}, total={}, status=PENDING", savedOrder.getId(), total);

        // Publish event for async processing
        eventBus.publish(RabbitMqConfig.ROUTING_KEY_CREATED, new OrderCreatedEvent(
                savedOrder.getId(),
                userId,
                savedOrder.getTotal(),
                LocalDateTime.now()
        ));

        // Re-fetch with eager-loaded items and products to avoid LazyInitializationException
        return orderRepository.findByIdWithItems(savedOrder.getId()).orElse(savedOrder);
    }

    /**
     * Retrieves all orders.
     *
     * @return a list of all orders
     */
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        log.debug("Finding all orders");
        return orderRepository.findAllWithItems();
    }

    /**
     * Retrieves all orders for a specific user.
     *
     * @param userId the user ID
     * @return a list of orders for the user
     */
    @Transactional(readOnly = true)
    public List<Order> findByUserId(Long userId) {
        log.debug("Finding orders for user: {}", userId);
        return orderRepository.findByUserIdWithItems(userId);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param id the order ID
     * @return the order entity
     * @throws ResourceNotFoundException if no order is found with the given ID
     */
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        log.debug("Finding order by id: {}", id);
        return orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    /**
     * Updates an order's status.
     *
     * <p>Validates that the status transition is allowed before updating.</p>
     *
     * @param id      the order ID
     * @param request the update request with new status
     * @return the updated order entity
     * @throws ResourceNotFoundException if no order is found with the given ID
     * @throws ValidationException       if the status transition is invalid
     */
    public Order update(Long id, UpdateOrderRequest request) {
        log.debug("Updating order status: id={}, newStatus={}", id, request.status());
        Order order = findById(id);
        validateStatusTransition(order.getStatus(), request.status());
        order.setStatus(request.status());
        Order savedOrder = orderRepository.save(order);
        log.debug("Order status updated: id={}, status={}", id, request.status());
        return savedOrder;
    }

    /**
     * Deletes an order by its ID.
     *
     * @param id the order ID
     * @throws ResourceNotFoundException if no order is found with the given ID
     */
    public void delete(Long id) {
        log.debug("Deleting order with id: {}", id);
        Order order = findById(id);
        orderRepository.delete(order);
        log.debug("Order deleted: id={}", id);
    }

    /**
     * Validates that a status transition is allowed.
     *
     * <p>Valid transitions:</p>
     * <ul>
     *   <li>PENDING → PROCESSING or EXPIRED</li>
     *   <li>PROCESSING → COMPLETED or EXPIRED</li>
     *   <li>COMPLETED → none (terminal)</li>
     *   <li>EXPIRED → none (terminal)</li>
     * </ul>
     *
     * @param currentStatus the current order status
     * @param newStatus     the requested new status
     * @throws ValidationException if the transition is not allowed
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean validTransition = switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.EXPIRED;
            case PROCESSING -> newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.EXPIRED;
            case COMPLETED, EXPIRED -> false;
        };

        if (!validTransition) {
            log.warn("Invalid status transition attempted: {} -> {}", currentStatus, newStatus);
            throw new ValidationException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }
    }
}
