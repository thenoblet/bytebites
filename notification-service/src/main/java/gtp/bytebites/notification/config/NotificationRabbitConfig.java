package gtp.bytebites.notification.config;

import gtp.bytebites.events.config.OrderEventConfig;
import gtp.bytebites.events.event.OrderPlacedEvent;
import gtp.bytebites.notification.listener.NotificationOrderListener;

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
public class NotificationRabbitConfig {
    private static final String NOTIFICATION_QUEUE = "notification.order.queue";

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true, false, false);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(orderExchange)
                .with(OrderEventConfig.ORDER_PLACED_ROUTING_KEY);
    }

    @Bean
    public MessageListenerAdapter notificationListenerAdapter(NotificationOrderListener listener) {
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
