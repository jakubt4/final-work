package com.gpustore.notification;

import com.gpustore.AbstractIntegrationTest;
import com.gpustore.config.RabbitMqConfig;
import com.gpustore.event.OrderCompletedEvent;
import com.gpustore.event.OrderExpiredEvent;
import com.gpustore.order.Order;
import com.gpustore.order.OrderRepository;
import com.gpustore.order.OrderStatus;
import com.gpustore.user.User;
import com.gpustore.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for NotificationService.
 */
class NotificationServiceIT extends AbstractIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user and order
        testUser = new User("Test User", "notification-test@example.com", "password");
        testUser = userRepository.save(testUser);

        testOrder = new Order(testUser, new BigDecimal("100.00"), OrderStatus.PROCESSING);
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    void handleOrderCompleted_shouldCreateEmailNotification() {
        // Given
        OrderCompletedEvent event = new OrderCompletedEvent(
                testOrder.getId(),
                testUser.getId(),
                testOrder.getTotal(),
                LocalDateTime.now()
        );

        // When
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                RabbitMqConfig.ROUTING_KEY_COMPLETED,
                event
        );

        // Then
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> !notificationRepository.findByOrderId(testOrder.getId()).isEmpty());

        List<Notification> notifications = notificationRepository.findByOrderId(testOrder.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.EMAIL);
        assertThat(notifications.get(0).getMessage()).contains("completed successfully");
    }

    @Test
    void handleOrderExpired_shouldCreateSystemAlertNotification() {
        // Given
        OrderExpiredEvent event = new OrderExpiredEvent(
                testOrder.getId(),
                testUser.getId(),
                "Processing timeout",
                LocalDateTime.now()
        );

        // When
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                RabbitMqConfig.ROUTING_KEY_EXPIRED,
                event
        );

        // Then
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> !notificationRepository.findByOrderId(testOrder.getId()).isEmpty());

        List<Notification> notifications = notificationRepository.findByOrderId(testOrder.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.SYSTEM_ALERT);
        assertThat(notifications.get(0).getMessage()).contains("expired");
    }
}
