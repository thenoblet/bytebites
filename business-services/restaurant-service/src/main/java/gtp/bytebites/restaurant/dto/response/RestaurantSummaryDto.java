package gtp.bytebites.restaurant.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record RestaurantSummaryDto(
        String name,
        String address,
        String cuisineType
) {
}
