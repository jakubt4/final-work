package com.gpustore.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

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
