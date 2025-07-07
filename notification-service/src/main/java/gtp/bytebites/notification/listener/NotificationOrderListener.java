package gtp.bytebites.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import gtp.bytebites.events.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NotificationOrderListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationOrderListener.class);

    public void handleMessage(byte[] messageBytes) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            OrderPlacedEvent event = mapper.readValue(messageBytes, OrderPlacedEvent.class);
            processOrderEvent(event);
        } catch (IOException e) {
            log.error("Failed to deserialize message", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        processOrderEvent(event);
    }

    private void processOrderEvent(OrderPlacedEvent event) {
        log.info("Notification Service received order: {}", event);
    }
}