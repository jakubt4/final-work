package com.gpustore.event;

import com.gpustore.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ implementation of the EventBus interface.
 *
 * <p>Publishes domain events to the orders exchange using the configured
 * JSON message converter.</p>
 */
@Service
public class RabbitMqEventBus implements EventBus {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqEventBus.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqEventBus(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(String routingKey, Object event) {
        log.info("Publishing event [{}]: {}", routingKey, event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, routingKey, event);
        log.debug("Event published successfully to exchange={}, routingKey={}",
                RabbitMqConfig.EXCHANGE_NAME, routingKey);
    }
}
