package com.gpustore.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "At least one item is required")
        @Valid
        List<OrderItemRequest> items
) {
}
