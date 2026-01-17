package com.gpustore.order.dto;

import com.gpustore.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderRequest(
        @NotNull(message = "Status is required")
        OrderStatus status
) {
}
