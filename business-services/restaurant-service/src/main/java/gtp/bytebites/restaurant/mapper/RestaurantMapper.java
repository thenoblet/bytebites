package gtp.bytebites.restaurant.mapper;

import gtp.bytebites.restaurant.dto.request.CreateRestaurantRequest;
import gtp.bytebites.restaurant.dto.response.RestaurantDto;
import gtp.bytebites.restaurant.dto.response.RestaurantSummaryDto;
import gtp.bytebites.restaurant.model.Restaurant;
import org.springframework.stereotype.Component;

@Component
public class RestaurantMapper {
    public RestaurantDto toDto(Restaurant restaurant) {
        if (restaurant == null) return null;
        return new RestaurantDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getDescription(),
                restaurant.getCuisineType(),
                restaurant.getCreatedAt(),
                restaurant.getUpdatedAt()

        );
    }

    public RestaurantSummaryDto toSummaryDto(Restaurant restaurant) {
        if (restaurant == null) return null;
        return new RestaurantSummaryDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getCuisineType()
        );
    }

    public Restaurant toEntity(CreateRestaurantRequest request) {
        if (request == null) return null;
        return Restaurant.builder()
                .name(request.name())
                .address(request.address())
                .owner(request.ownerId())
                .cuisineType(request.cuisineType())
                .menuItems(request.menuItems())
                .build();
    }
}