package gtp.bytebites.order.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDto(UUID menuItemId, String menuItemName, int quantity, BigDecimal price) {}
