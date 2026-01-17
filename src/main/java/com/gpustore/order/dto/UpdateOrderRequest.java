package com.gpustore.order.dto;

import com.gpustore.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating an order's status.
 *
 * @param status the new order status (required)
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record UpdateOrderRequest(
        @NotNull(message = "Status is required")
        OrderStatus status
) {
}
