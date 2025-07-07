package gtp.bytebites.order.event;

import gtp.bytebites.events.config.OrderEventConfig;
import gtp.bytebites.events.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes an OrderPlacedEvent to the central order exchange.
     * This single method is the only place that needs to know about exchanges
     * and routing keys for this event.
     * @param event The event payload to publish.
     */
    public void publishOrderPlaced(OrderPlacedEvent event) {
        log.info("Publishing OrderPlacedEvent for Order ID: {}", event.orderId());
        rabbitTemplate.convertAndSend(
                OrderEventConfig.ORDER_EXCHANGE,
                OrderEventConfig.ORDER_PLACED_ROUTING_KEY,
                event
        );
    }
}
