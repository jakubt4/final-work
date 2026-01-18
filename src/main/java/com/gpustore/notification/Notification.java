package com.gpustore.notification;

import com.gpustore.common.BaseEntity;
import com.gpustore.order.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entity representing a notification/audit log entry.
 * Created when orders reach terminal states (COMPLETED, EXPIRED).
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    /**
     * Default constructor for JPA.
     */
    protected Notification() {}

    /**
     * Creates a new notification.
     *
     * @param order   the order this notification relates to
     * @param type    the type of notification
     * @param message the notification message
     */
    public Notification(Order order, NotificationType type, String message) {
        this.order = order;
        this.type = type;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
