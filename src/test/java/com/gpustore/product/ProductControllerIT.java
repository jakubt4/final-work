package com.gpustore.product;

import com.gpustore.AbstractIntegrationTest;
import com.gpustore.product.dto.CreateProductRequest;
import com.gpustore.product.dto.ProductResponse;
import com.gpustore.product.dto.UpdateProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createProduct_WithValidData_ReturnsCreatedProduct() {
        // Given
        String token = getAuthToken();
        CreateProductRequest request = new CreateProductRequest(
                "Test GPU",
                "A powerful test GPU",
                new BigDecimal("999.99"),
                50
        );

        // When
        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                ProductResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Test GPU");
        assertThat(response.getBody().description()).isEqualTo("A powerful test GPU");
        assertThat(response.getBody().price()).isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(response.getBody().stock()).isEqualTo(50);
        assertThat(response.getBody().createdAt()).isNotNull();
    }

    @Test
    void getAllProducts_ReturnsSeededData() {
        // Given
        String token = getAuthToken();

        // When
        ResponseEntity<List> response = restTemplate.exchange(
                "/api/products",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                List.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // Seed data contains 12 GPU products
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(12);
    }

    @Test
    void getProductById_WithValidId_ReturnsProduct() {
        // Given
        String token = getAuthToken();
        // Get first seeded product
        Product firstProduct = productRepository.findAll().get(0);

        // When
        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products/" + firstProduct.getId(),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                ProductResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(firstProduct.getId());
        assertThat(response.getBody().name()).isEqualTo(firstProduct.getName());
    }

    @Test
    void updateProduct_WithValidData_ReturnsUpdatedProduct() {
        // Given
        String token = getAuthToken();
        // Create a new product to update
        CreateProductRequest createRequest = new CreateProductRequest(
                "Update Test GPU",
                "Original description",
                new BigDecimal("599.99"),
                30
        );
        ResponseEntity<ProductResponse> createResponse = restTemplate.exchange(
                "/api/products",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, authHeaders(token)),
                ProductResponse.class
        );
        Long productId = createResponse.getBody().id();

        // When - update the product
        UpdateProductRequest updateRequest = new UpdateProductRequest(
                "Updated GPU Name",
                "Updated description",
                new BigDecimal("699.99"),
                40
        );
        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products/" + productId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, authHeaders(token)),
                ProductResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Updated GPU Name");
        assertThat(response.getBody().description()).isEqualTo("Updated description");
        assertThat(response.getBody().price()).isEqualByComparingTo(new BigDecimal("699.99"));
        assertThat(response.getBody().stock()).isEqualTo(40);
    }

    @Test
    void deleteProduct_WithValidId_ReturnsNoContent() {
        // Given
        String token = getAuthToken();
        // Create a product to delete
        CreateProductRequest createRequest = new CreateProductRequest(
                "Delete Test GPU",
                "To be deleted",
                new BigDecimal("199.99"),
                10
        );
        ResponseEntity<ProductResponse> createResponse = restTemplate.exchange(
                "/api/products",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, authHeaders(token)),
                ProductResponse.class
        );
        Long productId = createResponse.getBody().id();

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/products/" + productId,
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(token)),
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify product is deleted
        assertThat(productRepository.findById(productId)).isEmpty();
    }

    @Test
    void getProductById_WithInvalidId_ReturnsNotFound() {
        // Given
        String token = getAuthToken();

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/99999",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
