package gtp.bytebites.restaurant.dto.request;

import gtp.bytebites.restaurant.model.Restaurant;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateMenuRequest(
        String name,

        @Size(max = 1000, message = "Description must be under 1000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
        BigDecimal price,

        @NotNull(message = "Restaurant name is required")
        Restaurant restaurant
) {
}
