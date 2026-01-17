package com.gpustore.product;

import com.gpustore.common.exception.ResourceNotFoundException;
import com.gpustore.common.exception.ValidationException;
import com.gpustore.product.dto.CreateProductRequest;
import com.gpustore.product.dto.UpdateProductRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product create(CreateProductRequest request) {
        Product product = new Product(
                request.name(),
                request.description(),
                request.price(),
                request.stock()
        );

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public Product update(Long id, UpdateProductRequest request) {
        Product product = findById(id);

        if (request.name() != null) {
            product.setName(request.name());
        }

        if (request.description() != null) {
            product.setDescription(request.description());
        }

        if (request.price() != null) {
            if (request.price().signum() < 0) {
                throw new ValidationException("Price must be at least 0.00");
            }
            product.setPrice(request.price());
        }

        if (request.stock() != null) {
            if (request.stock() < 0) {
                throw new ValidationException("Stock must be at least 0");
            }
            product.setStock(request.stock());
        }

        return productRepository.save(product);
    }

    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }
}
