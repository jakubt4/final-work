package com.gpustore.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for order event messaging.
 *
 * <p>Topology:</p>
 * <ul>
 *   <li>Exchange: orders.exchange (direct)</li>
 *   <li>Queue: orders.created.queue - consumed by OrderProcessor</li>
 *   <li>Queue: orders.completed.queue - consumed by NotificationService</li>
 *   <li>Queue: orders.expired.queue - consumed by NotificationService</li>
 *   <li>Queue: orders.dlq - dead letter queue for failed messages</li>
 * </ul>
 */
@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_NAME = "orders.exchange";
    public static final String CREATED_QUEUE = "orders.created.queue";
    public static final String COMPLETED_QUEUE = "orders.completed.queue";
    public static final String EXPIRED_QUEUE = "orders.expired.queue";
    public static final String DLQ_QUEUE = "orders.dlq";

    public static final String ROUTING_KEY_CREATED = "order.created";
    public static final String ROUTING_KEY_COMPLETED = "order.completed";
    public static final String ROUTING_KEY_EXPIRED = "order.expired";

    // ==================== Exchange ====================

    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    // ==================== Queues ====================

    @Bean
    public Queue ordersCreatedQueue() {
        return QueueBuilder.durable(CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DLQ_QUEUE)
                .build();
    }

    @Bean
    public Queue ordersCompletedQueue() {
        return QueueBuilder.durable(COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DLQ_QUEUE)
                .build();
    }

    @Bean
    public Queue ordersExpiredQueue() {
        return QueueBuilder.durable(EXPIRED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DLQ_QUEUE)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    // ==================== Bindings ====================

    @Bean
    public Binding createdBinding(Queue ordersCreatedQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(ordersCreatedQueue)
                .to(ordersExchange)
                .with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding completedBinding(Queue ordersCompletedQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(ordersCompletedQueue)
                .to(ordersExchange)
                .with(ROUTING_KEY_COMPLETED);
    }

    @Bean
    public Binding expiredBinding(Queue ordersExpiredQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(ordersExpiredQueue)
                .to(ordersExchange)
                .with(ROUTING_KEY_EXPIRED);
    }

    // ==================== Message Converter ====================

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
