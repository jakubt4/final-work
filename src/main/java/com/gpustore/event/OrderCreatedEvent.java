package com.gpustore.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain event published when a new order is created.
 * Triggers asynchronous order processing workflow.
 *
 * @param orderId   the unique identifier of the created order
 * @param userId    the ID of the user who placed the order
 * @param total     the total amount of the order
 * @param timestamp when the event occurred
 */
public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        BigDecimal total,
        LocalDateTime timestamp
) {}
