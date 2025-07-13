package gtp.restaurant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gtp.bytebites.events.event.OrderPlacedEvent;
import gtp.bytebites.restaurant.listener.RestaurantOrderListener;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantOrderListenerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestaurantOrderListener listener = new RestaurantOrderListener();

    @Test
    void handleMessage_shouldProcessValidOrderEvent() throws JsonProcessingException {
        // Given
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(),
                "customer-123",
                UUID.randomUUID(),
                BigDecimal.valueOf(29.99),
                Collections.singletonList(
                        new OrderPlacedEvent.OrderItemData(
                                UUID.randomUUID(),
                                "Pizza Margherita",
                                2
                        )
                )
        );

        byte[] messageBytes = objectMapper.writeValueAsBytes(event);

        // When
        listener.handleMessage(messageBytes);
    }

    @Test
    void handleMessage_shouldThrowAmqpRejectForInvalidMessage() {
        // Given
        byte[] invalidMessage = "invalid json".getBytes();

        // When/Then
        assertThrows(AmqpRejectAndDontRequeueException.class, () ->
                listener.handleMessage(invalidMessage));
    }

    @Test
    void handleMessage_shouldThrowAmqpRejectForMalformedEvent() throws JsonProcessingException {
        String malformedJson = """
            {
                "orderId": "invalid-uuid",
                "customerId": 12345,
                "missingField": "value"
            }
            """;

        byte[] malformedMessage = malformedJson.getBytes();

        // When/Then
        assertThrows(AmqpRejectAndDontRequeueException.class, () ->
                listener.handleMessage(malformedMessage));
    }

    @Test
    void handleOrderPlacedEvent_shouldProcessCompleteOrder() {
        // Given
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(),
                "customer-456",
                UUID.randomUUID(),
                BigDecimal.valueOf(45.50),
                List.of(
                        new OrderPlacedEvent.OrderItemData(
                                UUID.randomUUID(),
                                "Spaghetti Carbonara",
                                1
                        ),
                        new OrderPlacedEvent.OrderItemData(
                                UUID.randomUUID(),
                                "Tiramisu",
                                2
                        )
                )
        );

        // When
        listener.handleOrderPlacedEvent(event);
    }

    @Test
    void handleOrderPlacedEvent_shouldProcessEmptyOrder() {
        // Given
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(),
                "customer-789",
                UUID.randomUUID(),
                BigDecimal.ZERO,
                Collections.emptyList()
        );

        // When
        listener.handleOrderPlacedEvent(event);
    }

    @Test
    void handleOrderPlacedEvent_withLargeOrder_shouldProcessSuccessfully() {
        // Given
        List<OrderPlacedEvent.OrderItemData> items = IntStream.range(0, 100)
                .mapToObj(i -> new OrderPlacedEvent.OrderItemData(
                        UUID.randomUUID(),
                        "Item " + i,
                        1
                ))
                .toList();

        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(),
                "bulk-customer",
                UUID.randomUUID(),
                new BigDecimal("500.00"),
                items
        );

        // When
        long startTime = System.currentTimeMillis();
        listener.handleOrderPlacedEvent(event);
        long duration = System.currentTimeMillis() - startTime;

        // Then
        assertTrue(duration < 1000, "Processing should complete within 1 second");
    }
}