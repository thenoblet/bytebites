package gtp.bytebites.notification.listener;

import gtp.bytebites.events.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationOrderListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationOrderListener.class);

    /**
     * This single annotation replaces the entire MessageListenerAdapter and container setup.
     *
     * How it works:
     * 1. Spring detects this annotation and automatically creates a SimpleMessageListenerContainer.
     * 2. The container listens on the specified queue: "notification.order.queue".
     * 3. When a message arrives, it uses the globally defined MessageConverter
     * (the Jackson2JsonMessageConverter bean) to deserialize the JSON payload.
     * 4. It intelligently matches the deserialized object to the method's parameter type (OrderPlacedEvent).
     * 5. It invokes this method with the fully formed OrderPlacedEvent object.
     *
     * @param event The deserialized event object from the queue.
     */
    @RabbitListener(queues = "notification.order.queue")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Notification Service received order: {}", event);

        log.info("Simulating sending notification to customer {} for Order ID: {}", event.customerId(), event.orderId());
    }
}