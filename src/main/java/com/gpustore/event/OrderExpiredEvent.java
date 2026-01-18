package com.gpustore.event;

import java.time.LocalDateTime;

/**
 * Domain event published when an order expires due to processing timeout.
 * Triggers system alert notification and audit logging.
 *
 * @param orderId   the unique identifier of the expired order
 * @param userId    the ID of the user who owns the order
 * @param reason    human-readable reason for expiration
 * @param timestamp when the event occurred
 */
public record OrderExpiredEvent(
        Long orderId,
        Long userId,
        String reason,
        LocalDateTime timestamp
) {}
