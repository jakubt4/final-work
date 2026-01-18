package com.gpustore.event;

/**
 * Abstraction for publishing domain events.
 * Allows decoupling event producers from specific messaging infrastructure.
 *
 * <p>Implementations may use RabbitMQ, Kafka, or in-memory for testing.</p>
 */
public interface EventBus {

    /**
     * Publishes an event to the messaging infrastructure.
     *
     * @param routingKey the routing key determining which queue receives the event
     * @param event      the domain event object (must be JSON-serializable)
     */
    void publish(String routingKey, Object event);
}
