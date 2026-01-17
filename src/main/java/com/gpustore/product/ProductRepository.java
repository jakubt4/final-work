package com.gpustore.product;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Product} entities.
 *
 * <p>Provides CRUD operations and custom query methods for product management,
 * including pessimistic locking for concurrent stock updates.</p>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds a product by ID with pessimistic write lock.
     *
     * <p>Used for stock updates during order processing to prevent race conditions.</p>
     *
     * @param id the product ID
     * @return an Optional containing the locked product if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
}
