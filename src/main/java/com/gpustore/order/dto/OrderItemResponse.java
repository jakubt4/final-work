package com.gpustore.order.dto;

import com.gpustore.order.OrderItem;

import java.math.BigDecimal;

/**
 * Response DTO representing an order item.
 *
 * @param id          the order item's unique identifier
 * @param productId   the product ID
 * @param productName the product name at time of order
 * @param quantity    the quantity ordered
 * @param price       the unit price at time of order
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal price
) {
    /**
     * Creates an OrderItemResponse from an OrderItem entity.
     *
     * @param item the order item entity to convert
     * @return the response DTO
     */
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}
