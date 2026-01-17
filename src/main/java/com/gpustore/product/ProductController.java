package com.gpustore.product;

import com.gpustore.product.dto.CreateProductRequest;
import com.gpustore.product.dto.ProductResponse;
import com.gpustore.product.dto.UpdateProductRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for product management operations.
 *
 * <p>Provides endpoints for CRUD operations on products:</p>
 * <ul>
 *   <li>{@code POST /api/products} - Create a new product</li>
 *   <li>{@code GET /api/products} - List all products</li>
 *   <li>{@code GET /api/products/{id}} - Get product by ID</li>
 *   <li>{@code PUT /api/products/{id}} - Update product</li>
 *   <li>{@code DELETE /api/products/{id}} - Delete product</li>
 * </ul>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    /**
     * Constructs a new ProductController with the required service.
     *
     * @param productService the service for product operations
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Creates a new product.
     *
     * @param request the product creation request
     * @return 201 Created with the created product details
     */
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        log.info("Creating new product: {}", request.name());
        Product product = productService.create(request);
        log.info("Product created successfully: id={}", product.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(product));
    }

    /**
     * Retrieves all products.
     *
     * @return 200 OK with a list of all products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAll() {
        log.debug("Fetching all products");
        List<ProductResponse> products = productService.findAll().stream()
                .map(ProductResponse::from)
                .toList();
        log.debug("Found {} products", products.size());
        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the product ID
     * @return 200 OK with the product details, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        log.debug("Fetching product with id: {}", id);
        Product product = productService.findById(id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    /**
     * Updates an existing product.
     *
     * @param id      the product ID
     * @param request the update request
     * @return 200 OK with the updated product details, or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateProductRequest request) {
        log.info("Updating product with id: {}", id);
        Product product = productService.update(id, request);
        log.info("Product updated successfully: id={}", id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id the product ID
     * @return 204 No Content on success, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting product with id: {}", id);
        productService.delete(id);
        log.info("Product deleted successfully: id={}", id);
        return ResponseEntity.noContent().build();
    }
}
