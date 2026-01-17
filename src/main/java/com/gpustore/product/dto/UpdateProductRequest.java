package com.gpustore.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing product.
 *
 * <p>All fields are optional. Only non-null fields will be updated.</p>
 *
 * @param name        the new product name (optional, max 100 chars)
 * @param description the new product description (optional)
 * @param price       the new product price (optional, min 0.00)
 * @param stock       the new stock quantity (optional, min 0)
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record UpdateProductRequest(
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        String description,

        @DecimalMin(value = "0.00", message = "Price must be at least 0.00")
        BigDecimal price,

        @Min(value = 0, message = "Stock must be at least 0")
        Integer stock
) {
}
