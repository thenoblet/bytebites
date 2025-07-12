package gtp.bytebites.restaurant.dto.response;

import java.util.UUID;

public record RestaurantSummaryDto(
        UUID id,
        String name,
        String address,
        String cuisineType
) {
}
