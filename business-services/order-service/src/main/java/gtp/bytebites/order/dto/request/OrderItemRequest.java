package gtp.bytebites.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemRequest(
        @NotNull UUID menuItemId,
        String menuItemName,
        @Positive int quantity,
        @Positive BigDecimal price
) {
}
