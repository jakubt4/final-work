package com.gpustore.order;

import com.gpustore.AbstractIntegrationTest;
import com.gpustore.notification.NotificationRepository;
import com.gpustore.notification.NotificationType;
import com.gpustore.user.User;
import com.gpustore.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for OrderExpirationJob.
 */
class OrderExpirationJobIT extends AbstractIntegrationTest {

    @Autowired
    private OrderExpirationJob orderExpirationJob;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User("Test User", "expiration-test@example.com", "password");
        testUser = userRepository.save(testUser);
    }

    @Test
    void expireStaleOrders_shouldExpireProcessingOrdersOlderThan10Minutes() {
        // Given - order in PROCESSING state with old updatedAt
        Order staleOrder = new Order(testUser, new BigDecimal("100.00"), OrderStatus.PROCESSING);
        staleOrder = orderRepository.save(staleOrder);

        // Manually set updatedAt to 11 minutes ago
        LocalDateTime elevenMinutesAgo = LocalDateTime.now().minusMinutes(11);
        orderRepository.flush();
        ReflectionTestUtils.setField(staleOrder, "updatedAt", elevenMinutesAgo);
        orderRepository.saveAndFlush(staleOrder);

        // When
        orderExpirationJob.expireStaleOrders();

        // Then
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> {
                    Order reloaded = orderRepository.findById(staleOrder.getId()).orElseThrow();
                    return reloaded.getStatus() == OrderStatus.EXPIRED;
                });

        // Verify notification was created
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> !notificationRepository.findByOrderId(staleOrder.getId()).isEmpty());

        assertThat(notificationRepository.findByOrderId(staleOrder.getId()))
                .anyMatch(n -> n.getType() == NotificationType.SYSTEM_ALERT);
    }

    @Test
    void expireStaleOrders_shouldNotExpireRecentProcessingOrders() {
        // Given - recent order in PROCESSING state
        Order recentOrder = new Order(testUser, new BigDecimal("100.00"), OrderStatus.PROCESSING);
        recentOrder = orderRepository.save(recentOrder);

        // When
        orderExpirationJob.expireStaleOrders();

        // Then - order should still be PROCESSING
        Order reloaded = orderRepository.findById(recentOrder.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void expireStaleOrders_shouldNotExpirePendingOrders() {
        // Given - old PENDING order
        Order pendingOrder = new Order(testUser, new BigDecimal("100.00"), OrderStatus.PENDING);
        pendingOrder = orderRepository.save(pendingOrder);

        // Manually set updatedAt to 11 minutes ago
        ReflectionTestUtils.setField(pendingOrder, "updatedAt", LocalDateTime.now().minusMinutes(11));
        orderRepository.saveAndFlush(pendingOrder);

        // When
        orderExpirationJob.expireStaleOrders();

        // Then - PENDING orders should not be affected
        Order reloaded = orderRepository.findById(pendingOrder.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.PENDING);
    }
}
