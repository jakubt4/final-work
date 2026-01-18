package com.gpustore.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    /**
     * Finds all orders with items and products eagerly loaded.
     *
     * @return a list of all orders with their items and products
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product")
    List<Order> findAllWithItems();

    /**
     * Finds all orders for a user with items and products eagerly loaded.
     *
     * @param userId the user ID
     * @return a list of orders with their items and products
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.user.id = :userId")
    List<Order> findByUserIdWithItems(Long userId);

    /**
     * Finds an order by ID with items and products eagerly loaded.
     *
     * @param id the order ID
     * @return the order with its items and products
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id = :id")
    Optional<Order> findByIdWithItems(Long id);

    /**
     * Finds orders with a specific status that haven't been updated since the cutoff time.
     * Used by the expiration job to find stale PROCESSING orders.
     *
     * @param status the order status to filter by
     * @param cutoff orders with updatedAt before this time are returned
     * @return list of stale orders
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user WHERE o.status = :status AND o.updatedAt < :cutoff")
    List<Order> findByStatusAndUpdatedAtBefore(@Param("status") OrderStatus status,
                                                @Param("cutoff") LocalDateTime cutoff);
}
