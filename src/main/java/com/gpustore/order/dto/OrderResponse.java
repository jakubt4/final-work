package com.gpustore.order.dto;

import com.gpustore.order.Order;
import com.gpustore.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO representing order data returned by the API.
 *
 * @param id        the order's unique identifier
 * @param userId    the ID of the user who placed the order
 * @param total     the total order amount
 * @param status    the current order status
 * @param items     the list of order items
 * @param createdAt when the order was created
 * @param updatedAt when the order was last updated
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record OrderResponse(
        Long id,
        Long userId,
        BigDecimal total,
        OrderStatus status,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Creates an OrderResponse from an Order entity.
     *
     * @param order the order entity to convert
     * @return the response DTO
     */
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getTotal(),
                order.getStatus(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
