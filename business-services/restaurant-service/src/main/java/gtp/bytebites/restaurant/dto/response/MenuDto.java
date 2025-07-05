package gtp.bytebites.restaurant.dto.response;

import gtp.bytebites.restaurant.model.Restaurant;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MenuDto(
        String name,
        String description,
        BigDecimal price,
        Restaurant restaurant,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
