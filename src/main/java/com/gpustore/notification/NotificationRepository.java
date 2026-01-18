package com.gpustore.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Notification entities.
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Finds all notifications for a specific order.
     *
     * @param orderId the order ID
     * @return list of notifications for the order
     */
    List<Notification> findByOrderId(Long orderId);

    /**
     * Finds all notifications of a specific type.
     *
     * @param type the notification type
     * @return list of notifications of the given type
     */
    List<Notification> findByType(NotificationType type);
}
