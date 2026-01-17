package com.gpustore.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for an individual order item.
 *
 * @param productId the ID of the product to order (required)
 * @param quantity  the quantity to order (required, min 1)
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record OrderItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {
}
