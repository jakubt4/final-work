package com.gpustore.order;

import com.gpustore.order.dto.CreateOrderRequest;
import com.gpustore.order.dto.OrderResponse;
import com.gpustore.order.dto.UpdateOrderRequest;
import com.gpustore.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                 @Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll() {
        List<OrderResponse> orders = orderService.findAll().stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        Order order = orderService.findById(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateOrderRequest request) {
        Order order = orderService.update(id, request);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
