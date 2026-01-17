package com.gpustore.product.dto;

import com.gpustore.product.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO representing product data returned by the API.
 *
 * @param id          the product's unique identifier
 * @param name        the product name
 * @param description the product description
 * @param price       the product price
 * @param stock       the current stock quantity
 * @param createdAt   when the product was created
 * @param updatedAt   when the product was last updated
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Creates a ProductResponse from a Product entity.
     *
     * @param product the product entity to convert
     * @return the response DTO
     */
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
