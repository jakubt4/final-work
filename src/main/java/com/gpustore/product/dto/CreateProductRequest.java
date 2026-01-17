package com.gpustore.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new product.
 *
 * @param name        the product name (required, max 100 chars)
 * @param description the product description (optional)
 * @param price       the product price (required, min 0.00)
 * @param stock       the initial stock quantity (required, min 0)
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record CreateProductRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.00", message = "Price must be at least 0.00")
        BigDecimal price,

        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock must be at least 0")
        Integer stock
) {
}
