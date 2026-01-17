package com.gpustore.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Order} entities.
 *
 * <p>Provides CRUD operations and custom query methods for order management.</p>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Finds all orders belonging to a specific user.
     *
     * @param userId the user ID
     * @return a list of orders for the user
     */
    List<Order> findByUserId(Long userId);
}
