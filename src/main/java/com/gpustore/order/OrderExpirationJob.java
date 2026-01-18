package com.gpustore.order;

import com.gpustore.config.RabbitMqConfig;
import com.gpustore.event.EventBus;
import com.gpustore.event.OrderExpiredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job that expires orders stuck in PROCESSING state.
 *
 * <p>Runs every 60 seconds and marks orders as EXPIRED if they have been
 * in PROCESSING state for more than 10 minutes.</p>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class OrderExpirationJob {

    private static final Logger log = LoggerFactory.getLogger(OrderExpirationJob.class);
    private static final int EXPIRATION_MINUTES = 10;

    private final OrderRepository orderRepository;
    private final EventBus eventBus;

    /**
     * Constructs a new OrderExpirationJob.
     *
     * @param orderRepository repository for order operations
     * @param eventBus        event bus for publishing expiration events
     */
    public OrderExpirationJob(OrderRepository orderRepository, EventBus eventBus) {
        this.orderRepository = orderRepository;
        this.eventBus = eventBus;
    }

    /**
     * Finds and expires stale orders.
     * Runs every 60 seconds.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireStaleOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(EXPIRATION_MINUTES);

        List<Order> staleOrders = orderRepository.findByStatusAndUpdatedAtBefore(
                OrderStatus.PROCESSING, cutoff
        );

        if (staleOrders.isEmpty()) {
            log.debug("No stale orders found for expiration");
            return;
        }

        log.info("Found {} stale orders to expire", staleOrders.size());

        for (Order order : staleOrders) {
            expireOrder(order);
        }
    }

    /**
     * Expires a single order and publishes an expiration event.
     *
     * @param order the order to expire
     */
    private void expireOrder(Order order) {
        order.setStatus(OrderStatus.EXPIRED);
        orderRepository.save(order);

        eventBus.publish(RabbitMqConfig.ROUTING_KEY_EXPIRED, new OrderExpiredEvent(
                order.getId(),
                order.getUser().getId(),
                "Processing timeout exceeded " + EXPIRATION_MINUTES + " minutes",
                LocalDateTime.now()
        ));

        log.info("Order {} expired due to processing timeout", order.getId());
    }
}
