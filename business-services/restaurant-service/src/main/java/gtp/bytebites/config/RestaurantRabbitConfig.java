package gtp.bytebites.config;

import gtp.bytebites.events.config.OrderEventConfig;
import gtp.bytebites.events.event.OrderPlacedEvent;
import gtp.bytebites.restaurant.listener.RestaurantOrderListener;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Import(OrderEventConfig.class)
public class RestaurantRabbitConfig {
    private static final String RESTAURANT_QUEUE = "restaurant.order.queue";

    @Bean
    public Queue restaurantQueue() {
        return new Queue(RESTAURANT_QUEUE, true, false, false);
    }

    @Bean
    public Binding restaurantBinding(Queue restaurantQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(restaurantQueue)
                .to(orderExchange)
                .with(OrderEventConfig.ORDER_PLACED_ROUTING_KEY);
    }

    @Bean
    public MessageListenerAdapter notificationListenerAdapter(RestaurantOrderListener listener) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(listener);

        adapter.setDefaultListenerMethod("handleMessage");

        adapter.setMessageConverter(OrderEventConfig.jsonMessageConverter());
        return adapter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("gtp.bytebites.events.event");
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("OrderPlacedEvent", OrderPlacedEvent.class);
        classMapper.setIdClassMapping(idClassMapping);
        return classMapper;
    }
}
