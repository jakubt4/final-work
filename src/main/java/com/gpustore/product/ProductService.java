package com.gpustore.product;

import com.gpustore.common.exception.ResourceNotFoundException;
import com.gpustore.common.exception.ValidationException;
import com.gpustore.product.dto.CreateProductRequest;
import com.gpustore.product.dto.UpdateProductRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for product management operations.
 *
 * <p>Handles business logic for product CRUD operations including
 * validation of prices and stock quantities.</p>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    /**
     * Constructs a new ProductService with required dependencies.
     *
     * @param productRepository the repository for product persistence
     */
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Creates a new product with the provided details.
     *
     * @param request the product creation request
     * @return the created product entity
     */
    public Product create(CreateProductRequest request) {
        log.debug("Creating product: {}", request.name());
        Product product = new Product(
                request.name(),
                request.description(),
                request.price(),
                request.stock()
        );

        Product savedProduct = productRepository.save(product);
        log.debug("Product created with id: {}", savedProduct.getId());
        return savedProduct;
    }

    /**
     * Retrieves all products.
     *
     * @return a list of all products
     */
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        log.debug("Finding all products");
        return productRepository.findAll();
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the product ID
     * @return the product entity
     * @throws ResourceNotFoundException if no product is found with the given ID
     */
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        log.debug("Finding product by id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    /**
     * Updates an existing product with the provided details.
     *
     * <p>Only non-null fields in the request are updated.</p>
     *
     * @param id      the product ID
     * @param request the update request
     * @return the updated product entity
     * @throws ResourceNotFoundException if no product is found with the given ID
     * @throws ValidationException       if price or stock values are invalid
     */
    public Product update(Long id, UpdateProductRequest request) {
        log.debug("Updating product with id: {}", id);
        Product product = findById(id);

        if (request.name() != null) {
            product.setName(request.name());
        }

        if (request.description() != null) {
            product.setDescription(request.description());
        }

        if (request.price() != null) {
            if (request.price().signum() < 0) {
                log.warn("Invalid price update attempted for product {}: {}", id, request.price());
                throw new ValidationException("Price must be at least 0.00");
            }
            product.setPrice(request.price());
        }

        if (request.stock() != null) {
            if (request.stock() < 0) {
                log.warn("Invalid stock update attempted for product {}: {}", id, request.stock());
                throw new ValidationException("Stock must be at least 0");
            }
            product.setStock(request.stock());
        }

        Product savedProduct = productRepository.save(product);
        log.debug("Product updated: id={}", id);
        return savedProduct;
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id the product ID
     * @throws ResourceNotFoundException if no product is found with the given ID
     */
    public void delete(Long id) {
        log.debug("Deleting product with id: {}", id);
        Product product = findById(id);
        productRepository.delete(product);
        log.debug("Product deleted: id={}", id);
    }
}
