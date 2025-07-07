package gtp.bytebites.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDto(
        UUID id,
        UUID restaurantId,
        String status,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        List<OrderItemDto> items
) {}
