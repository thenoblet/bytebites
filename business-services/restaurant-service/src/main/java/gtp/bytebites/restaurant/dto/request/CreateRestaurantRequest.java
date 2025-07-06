package gtp.bytebites.restaurant.dto.request;

import gtp.bytebites.restaurant.model.MenuItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateRestaurantRequest(
        @NotBlank(message = "Restaurant name cannot be blank")
        @Size(max = 200, message = "Restaurant name must be under 100 characters")
        String name,

        @Size(max = 255, message = "Address must be under 255 characters")
        String address,

        @Size(max = 1000, message = "Description must be under 1000 characters")
        String description,

        UUID ownerId,

        String cuisineType,

        List<MenuItem> menuItems
) {
}
