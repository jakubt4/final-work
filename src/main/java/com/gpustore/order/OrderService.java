package com.gpustore.order;

import com.gpustore.common.exception.ResourceNotFoundException;
import com.gpustore.common.exception.ValidationException;
import com.gpustore.order.dto.CreateOrderRequest;
import com.gpustore.order.dto.OrderItemRequest;
import com.gpustore.order.dto.UpdateOrderRequest;
import com.gpustore.product.Product;
import com.gpustore.product.ProductRepository;
import com.gpustore.user.User;
import com.gpustore.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Order create(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Order order = new Order(user, BigDecimal.ZERO, OrderStatus.PENDING);
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findByIdWithLock(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemRequest.productId()));

            if (product.getStock() < itemRequest.quantity()) {
                throw new ValidationException(
                        String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                                product.getName(), product.getStock(), itemRequest.quantity()));
            }

            product.setStock(product.getStock() - itemRequest.quantity());
            productRepository.save(product);

            BigDecimal itemPrice = product.getPrice();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));
            total = total.add(itemTotal);

            OrderItem orderItem = new OrderItem(order, product, itemRequest.quantity(), itemPrice);
            order.addItem(orderItem);
        }

        order.setTotal(total);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    public Order update(Long id, UpdateOrderRequest request) {
        Order order = findById(id);
        validateStatusTransition(order.getStatus(), request.status());
        order.setStatus(request.status());
        return orderRepository.save(order);
    }

    public void delete(Long id) {
        Order order = findById(id);
        orderRepository.delete(order);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean validTransition = switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.EXPIRED;
            case PROCESSING -> newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.EXPIRED;
            case COMPLETED, EXPIRED -> false;
        };

        if (!validTransition) {
            throw new ValidationException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }
    }
}
