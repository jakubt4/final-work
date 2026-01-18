package com.gpustore.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain event published when an order is successfully completed.
 * Triggers notification to user and audit logging.
 *
 * @param orderId   the unique identifier of the completed order
 * @param userId    the ID of the user who owns the order
 * @param total     the total amount charged
 * @param timestamp when the event occurred
 */
public record OrderCompletedEvent(
        Long orderId,
        Long userId,
        BigDecimal total,
        LocalDateTime timestamp
) {}
