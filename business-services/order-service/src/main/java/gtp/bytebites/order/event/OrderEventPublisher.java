package gtp.bytebites.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static gtp.bytebites.events.config.RabbitMQConfig.EXCHANGE_NAME;
import static gtp.bytebites.events.config.RabbitMQConfig.ROUTING_KEY;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderPlacedEvent(OrderPlacedEvent event) {
        try {
            log.info("Publishing OrderPlacedEvent for orderId: {}", event.orderId());
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event);
            log.info("Successfully published OrderPlacedEvent for orderId: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to publish OrderPlacedEvent for orderId: {}. Error: {}", event.orderId(), e.getMessage());
            // Nancy thinks: In a real system, you'd add retry logic or publish to a "failed events" queue here.
        }
    }
}