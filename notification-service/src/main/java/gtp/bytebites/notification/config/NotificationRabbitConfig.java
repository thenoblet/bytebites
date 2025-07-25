package gtp.bytebites.notification.config;

import gtp.bytebites.events.config.OrderEventConfig;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(OrderEventConfig.class)
public class NotificationRabbitConfig {

    private static final String NOTIFICATION_QUEUE = "notification.order.queue";

    /**
     * Defines the queue for the notification service. It's durable, so it
     * survives broker restarts.
     */
    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    /**
     * Binds the notification queue to the central order exchange using the
     * routing key for placed orders. This creates the subscription.
     * @param notificationQueue The queue bean for this service.
     * @param orderExchange The shared exchange bean from OrderEventConfig.
     */
    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(orderExchange)
                .with(OrderEventConfig.ORDER_PLACED_ROUTING_KEY);
    }
}
