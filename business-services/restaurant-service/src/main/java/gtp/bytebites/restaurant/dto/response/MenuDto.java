package gtp.bytebites.restaurant.dto.response;

import gtp.bytebites.restaurant.model.Restaurant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MenuDto(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        UUID restaurantId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
