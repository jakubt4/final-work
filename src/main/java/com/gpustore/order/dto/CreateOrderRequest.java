package com.gpustore.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for creating a new order.
 *
 * @param items the list of order items (required, at least one)
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record CreateOrderRequest(
        @NotEmpty(message = "At least one item is required")
        @Valid
        List<OrderItemRequest> items
) {
}
