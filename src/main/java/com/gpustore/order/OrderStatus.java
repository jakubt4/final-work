package com.gpustore.order;

/**
 * Enumeration of possible order statuses.
 *
 * <p>Order lifecycle:</p>
 * <ol>
 *   <li>{@link #PENDING} - Order created, awaiting processing</li>
 *   <li>{@link #PROCESSING} - Order is being processed</li>
 *   <li>{@link #COMPLETED} - Order has been fulfilled</li>
 *   <li>{@link #EXPIRED} - Order expired without completion</li>
 * </ol>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public enum OrderStatus {
    /** Order created, awaiting processing. */
    PENDING,
    /** Order is being processed. */
    PROCESSING,
    /** Order has been fulfilled. */
    COMPLETED,
    /** Order expired without completion. */
    EXPIRED
}
