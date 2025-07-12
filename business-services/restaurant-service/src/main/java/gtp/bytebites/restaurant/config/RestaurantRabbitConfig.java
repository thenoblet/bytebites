package gtp.bytebites.restaurant.config;

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
public class RestaurantRabbitConfig {
    private static final String RESTAURANT_QUEUE = "restaurant.order.queue";

    /**
     * Defines the queue for the restaurant service. It's durable, so it
     * survives broker restarts.
     */
    @Bean
    public Queue restaurantQueue() {
        return new Queue(RESTAURANT_QUEUE, true);
    }

    /**
     * Binds the restaurant queue to the central order exchange using the
     * routing key for placed orders. This creates the subscription.
     * @param restaurantQueue The queue bean for this service.
     * @param orderExchange The shared exchange bean from OrderEventConfig.
     */
    @Bean
    public Binding notificationBinding(Queue restaurantQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(restaurantQueue)
                .to(orderExchange)
                .with(OrderEventConfig.ORDER_PLACED_ROUTING_KEY);
    }
}