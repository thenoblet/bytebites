package gtp.bytebites.restaurant.dto.response;

import gtp.bytebites.restaurant.model.Restaurant;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MenuSummaryDto(
        String name,
        BigDecimal price,
        Restaurant restaurant
) {
}
