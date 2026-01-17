package com.gpustore.order;

import com.gpustore.AbstractIntegrationTest;
import com.gpustore.order.dto.CreateOrderRequest;
import com.gpustore.order.dto.OrderItemRequest;
import com.gpustore.order.dto.OrderResponse;
import com.gpustore.order.dto.UpdateOrderRequest;
import com.gpustore.product.Product;
import com.gpustore.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createOrder_WithValidData_ReducesProductStock() {
        // Given
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        int initialStock = product.getStock();
        int orderQuantity = 2;

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), orderQuantity))
        );

        // When
        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                OrderResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getBody().items()).hasSize(1);

        // Verify stock was reduced
        Product updatedProduct = productRepository.findById(product.getId()).get();
        assertThat(updatedProduct.getStock()).isEqualTo(initialStock - orderQuantity);
    }

    @Test
    void createOrder_WithInsufficientStock_ReturnsBadRequest() {
        // Given
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        int excessiveQuantity = product.getStock() + 100;

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), excessiveQuantity))
        );

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createOrder_TotalCalculatedCorrectly() {
        // Given
        String token = getAuthToken();
        List<Product> products = productRepository.findAll();
        Product product1 = products.get(0);
        Product product2 = products.get(1);

        int quantity1 = 2;
        int quantity2 = 3;

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(
                        new OrderItemRequest(product1.getId(), quantity1),
                        new OrderItemRequest(product2.getId(), quantity2)
                )
        );

        // When
        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                OrderResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        // Calculate expected total
        BigDecimal expectedTotal = product1.getPrice().multiply(BigDecimal.valueOf(quantity1))
                .add(product2.getPrice().multiply(BigDecimal.valueOf(quantity2)));

        assertThat(response.getBody().total()).isEqualByComparingTo(expectedTotal);
    }

    @Test
    void updateOrderStatus_FromPendingToProcessing_Works() {
        // Given - create an order first
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        CreateOrderRequest createRequest = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 1))
        );
        ResponseEntity<OrderResponse> createResponse = restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, authHeaders(token)),
                OrderResponse.class
        );
        Long orderId = createResponse.getBody().id();

        // When - update status to PROCESSING
        UpdateOrderRequest updateRequest = new UpdateOrderRequest(OrderStatus.PROCESSING);
        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                "/api/orders/" + orderId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, authHeaders(token)),
                OrderResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void getOrderById_WithValidId_ReturnsOrder() {
        // Given - create an order first
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        CreateOrderRequest createRequest = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 1))
        );
        ResponseEntity<OrderResponse> createResponse = restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, authHeaders(token)),
                OrderResponse.class
        );
        Long orderId = createResponse.getBody().id();

        // When
        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                "/api/orders/" + orderId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                OrderResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(orderId);
    }

    @Test
    void getAllOrders_WithAuthentication_ReturnsOrdersList() {
        // Given - create an order first
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        CreateOrderRequest createRequest = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 1))
        );
        restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, authHeaders(token)),
                OrderResponse.class
        );

        // When
        ResponseEntity<List> response = restTemplate.exchange(
                "/api/orders",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                List.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void deleteOrder_WithValidId_ReturnsNoContent() {
        // Given - create an order first
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        CreateOrderRequest createRequest = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 1))
        );
        ResponseEntity<OrderResponse> createResponse = restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, authHeaders(token)),
                OrderResponse.class
        );
        Long orderId = createResponse.getBody().id();

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/orders/" + orderId,
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(token)),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(orderRepository.findById(orderId)).isEmpty();
    }
}
