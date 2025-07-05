package gtp.bytebites.restaurant.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record RestaurantDto(
        UUID id,
        String name,
        String address,
        String description,
        String cuisineType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
