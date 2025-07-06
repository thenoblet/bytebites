package gtp.bytebites.order.client.dto;

import java.util.UUID;

public record RestaurantDto(
        UUID id,
        String owner
) {}
