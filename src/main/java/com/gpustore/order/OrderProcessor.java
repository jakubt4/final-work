package com.gpustore.order;

import com.gpustore.common.exception.ResourceNotFoundException;
import com.gpustore.config.RabbitMqConfig;
import com.gpustore.event.EventBus;
import com.gpustore.event.OrderCompletedEvent;
import com.gpustore.event.OrderCreatedEvent;
import com.gpustore.product.Product;
import com.gpustore.product.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Asynchronous order processor that consumes OrderCreatedEvent messages.
 *
 * <p>Processing flow:</p>
 * <ol>
 *   <li>Receives OrderCreatedEvent from orders.created.queue</li>
 *   <li>Updates order status to PROCESSING</li>
 *   <li>Simulates payment processing (5 second delay)</li>
 *   <li>50% success rate: on success, deducts stock and marks COMPLETED</li>
 *   <li>On failure, order remains in PROCESSING (scheduler will expire it)</li>
 * </ol>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class OrderProcessor {

    private static final Logger log = LoggerFactory.getLogger(OrderProcessor.class);
    private static final Random RANDOM = new Random();
    private static final int PAYMENT_SIMULATION_MS = 5000;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final EventBus eventBus;

    /**
     * Constructs a new OrderProcessor with required dependencies.
     *
     * @param orderRepository   the repository for order persistence
     * @param productRepository the repository for product operations
     * @param eventBus          the event bus for publishing domain events
     */
    public OrderProcessor(OrderRepository orderRepository,
                          ProductRepository productRepository,
                          EventBus eventBus) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.eventBus = eventBus;
    }

    /**
     * Processes an order asynchronously after creation.
     *
     * @param event the order created event containing order details
     */
    @RabbitListener(queues = RabbitMqConfig.CREATED_QUEUE)
    @Transactional
    public void processOrder(OrderCreatedEvent event) {
        log.info("Processing order: orderId={}", event.orderId());

        Order order = orderRepository.findByIdWithItems(event.orderId())
                .orElseThrow(() -> {
                    log.error("Order not found for processing: {}", event.orderId());
                    return new ResourceNotFoundException("Order", event.orderId());
                });

        // Idempotency check: only process PENDING orders
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order {} not in PENDING state (current={}), skipping processing",
                    event.orderId(), order.getStatus());
            return;
        }

        // Transition to PROCESSING
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
        log.info("Order {} status updated to PROCESSING", order.getId());

        // Simulate payment processing
        simulatePaymentProcessing();

        // 50% success rate simulation
        if (RANDOM.nextBoolean()) {
            completeOrder(order);
        } else {
            log.info("Order {} payment failed simulation, will be expired by scheduler",
                    order.getId());
            // Order stays in PROCESSING - scheduler will expire it after timeout
        }
    }

    /**
     * Simulates payment processing with a configurable delay.
     */
    private void simulatePaymentProcessing() {
        try {
            log.debug("Simulating payment processing ({} ms delay)", PAYMENT_SIMULATION_MS);
            Thread.sleep(PAYMENT_SIMULATION_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Payment simulation interrupted");
        }
    }

    /**
     * Completes an order by deducting stock and updating status.
     *
     * @param order the order to complete
     */
    private void completeOrder(Order order) {
        // Deduct stock for all items
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findByIdWithLock(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", item.getProduct().getId()));

            int newStock = product.getStock() - item.getQuantity();
            if (newStock < 0) {
                log.error("Insufficient stock for product {} during order {} completion",
                        product.getId(), order.getId());
                // In a real system, we might handle this differently (refund, partial fulfillment)
                // For now, we still complete but log the error
                newStock = 0;
            }
            product.setStock(newStock);
            productRepository.save(product);
            log.debug("Stock deducted for product {}: quantity={}, newStock={}",
                    product.getId(), item.getQuantity(), newStock);
        }

        // Mark as completed
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        // Publish completion event
        eventBus.publish(RabbitMqConfig.ROUTING_KEY_COMPLETED, new OrderCompletedEvent(
                order.getId(),
                order.getUser().getId(),
                order.getTotal(),
                LocalDateTime.now()
        ));

        log.info("Order {} completed successfully", order.getId());
    }
}
