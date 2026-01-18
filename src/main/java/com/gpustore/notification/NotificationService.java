package com.gpustore.notification;

import com.gpustore.common.exception.ResourceNotFoundException;
import com.gpustore.config.RabbitMqConfig;
import com.gpustore.event.OrderCompletedEvent;
import com.gpustore.event.OrderExpiredEvent;
import com.gpustore.order.Order;
import com.gpustore.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that listens to order events and creates audit notifications.
 *
 * <p>Handles the following events:</p>
 * <ul>
 *   <li>{@link OrderCompletedEvent} - Creates EMAIL notification</li>
 *   <li>{@link OrderExpiredEvent} - Creates SYSTEM_ALERT notification</li>
 * </ul>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final OrderRepository orderRepository;

    /**
     * Constructs a new NotificationService.
     *
     * @param notificationRepository repository for notification persistence
     * @param orderRepository        repository for order lookups
     */
    public NotificationService(NotificationRepository notificationRepository,
                               OrderRepository orderRepository) {
        this.notificationRepository = notificationRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Handles order completed events - creates EMAIL notification.
     *
     * @param event the order completed event
     */
    @RabbitListener(queues = RabbitMqConfig.COMPLETED_QUEUE)
    @Transactional
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("Handling order completed event: orderId={}", event.orderId());

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", event.orderId()));

        String message = String.format(
                "Order #%d completed successfully. Total amount: $%.2f. " +
                "Thank you for your purchase!",
                event.orderId(), event.total()
        );

        Notification notification = new Notification(order, NotificationType.EMAIL, message);
        notificationRepository.save(notification);

        // Simulate sending email (log only)
        log.info("EMAIL NOTIFICATION: To user {} - {}", event.userId(), message);
    }

    /**
     * Handles order expired events - creates SYSTEM_ALERT notification.
     *
     * @param event the order expired event
     */
    @RabbitListener(queues = RabbitMqConfig.EXPIRED_QUEUE)
    @Transactional
    public void handleOrderExpired(OrderExpiredEvent event) {
        log.info("Handling order expired event: orderId={}, reason={}",
                event.orderId(), event.reason());

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", event.orderId()));

        String message = String.format(
                "Order #%d has expired. Reason: %s. " +
                "Please contact support if you believe this is an error.",
                event.orderId(), event.reason()
        );

        Notification notification = new Notification(order, NotificationType.SYSTEM_ALERT, message);
        notificationRepository.save(notification);

        // Log system alert
        log.warn("SYSTEM ALERT: Order {} expired - {}", event.orderId(), event.reason());
    }
}
