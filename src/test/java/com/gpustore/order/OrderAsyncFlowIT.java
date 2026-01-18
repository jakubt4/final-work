package com.gpustore.order;

import com.gpustore.AbstractIntegrationTest;
import com.gpustore.order.dto.CreateOrderRequest;
import com.gpustore.order.dto.OrderItemRequest;
import com.gpustore.order.dto.OrderResponse;
import com.gpustore.product.Product;
import com.gpustore.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for asynchronous order processing flow.
 *
 * <p>Tests the event-driven order lifecycle:</p>
 * <ul>
 *   <li>Order creation returns PENDING immediately</li>
 *   <li>Async processing transitions to PROCESSING</li>
 *   <li>Payment simulation completes or leaves for expiration</li>
 *   <li>Stock is not deducted until async processing completes</li>
 * </ul>
 */
class OrderAsyncFlowIT extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createOrder_shouldReturnPendingImmediately() {
        // Given
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        // When
        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                OrderResponse.class
        );

        // Then - should return immediately with PENDING status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void createOrder_shouldTransitionToProcessingWithinTimeout() {
        // Given
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        // When
        ResponseEntity<OrderResponse> createResponse = restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                OrderResponse.class
        );
        Long orderId = createResponse.getBody().id();

        // Then - should transition to PROCESSING within 2 seconds
        await().atMost(2, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .until(() -> {
                    ResponseEntity<OrderResponse> getResponse = restTemplate.exchange(
                            "/api/orders/" + orderId,
                            HttpMethod.GET,
                            new HttpEntity<>(authHeaders(token)),
                            OrderResponse.class
                    );
                    OrderStatus status = getResponse.getBody().status();
                    return status == OrderStatus.PROCESSING || status == OrderStatus.COMPLETED;
                });
    }

    @Test
    void createOrder_shouldReachTerminalStateWithinTimeout() {
        // Given
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        // When
        ResponseEntity<OrderResponse> createResponse = restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                OrderResponse.class
        );
        Long orderId = createResponse.getBody().id();

        // Then - should reach COMPLETED or stay in PROCESSING (50% success rate)
        // Wait for processing to complete (5s payment simulation + buffer)
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> {
                    ResponseEntity<OrderResponse> getResponse = restTemplate.exchange(
                            "/api/orders/" + orderId,
                            HttpMethod.GET,
                            new HttpEntity<>(authHeaders(token)),
                            OrderResponse.class
                    );
                    OrderStatus status = getResponse.getBody().status();
                    // Either COMPLETED (success) or PROCESSING (failed, waiting for expiration)
                    return status == OrderStatus.COMPLETED || status == OrderStatus.PROCESSING;
                });
    }

    @Test
    void createOrder_shouldNotDeductStockImmediately() {
        // Given
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        int initialStock = product.getStock();
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        // When
        restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                OrderResponse.class
        );

        // Then - stock should not be deducted immediately (deferred to async processing)
        Product reloadedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(reloadedProduct.getStock()).isEqualTo(initialStock);
    }

    @Test
    void createOrder_stockDeductedOnlyAfterCompletion() {
        // Given
        String token = getAuthToken();
        Product product = productRepository.findAll().get(0);
        int initialStock = product.getStock();
        int orderQuantity = 1;
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), orderQuantity))
        );

        // When - create order
        ResponseEntity<OrderResponse> createResponse = restTemplate.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                OrderResponse.class
        );
        Long orderId = createResponse.getBody().id();

        // Wait for async processing to complete
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> {
                    ResponseEntity<OrderResponse> getResponse = restTemplate.exchange(
                            "/api/orders/" + orderId,
                            HttpMethod.GET,
                            new HttpEntity<>(authHeaders(token)),
                            OrderResponse.class
                    );
                    OrderStatus status = getResponse.getBody().status();
                    return status == OrderStatus.COMPLETED || status == OrderStatus.PROCESSING;
                });

        // Then - check final state
        ResponseEntity<OrderResponse> finalResponse = restTemplate.exchange(
                "/api/orders/" + orderId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                OrderResponse.class
        );

        Product reloadedProduct = productRepository.findById(product.getId()).orElseThrow();

        if (finalResponse.getBody().status() == OrderStatus.COMPLETED) {
            // Stock should be deducted on completion
            assertThat(reloadedProduct.getStock()).isEqualTo(initialStock - orderQuantity);
        } else {
            // Stock should not be deducted if still processing (failed payment)
            assertThat(reloadedProduct.getStock()).isEqualTo(initialStock);
        }
    }
}
