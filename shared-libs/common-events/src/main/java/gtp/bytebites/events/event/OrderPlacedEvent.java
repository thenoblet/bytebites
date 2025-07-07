package gtp.bytebites.events.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderPlacedEvent(
        UUID orderId,
        String customerId,
        UUID restaurantId,
        BigDecimal totalPrice,
        List<OrderItemData> items
) {
    public record OrderItemData(UUID menuItemId, String name, int quantity) {}
}
