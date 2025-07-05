package gtp.bytebites.restaurant.service;

import gtp.bytebites.restaurant.dto.response.RestaurantDto;
import gtp.bytebites.restaurant.dto.response.RestaurantSummaryDto;
import gtp.bytebites.restaurant.model.MenuItem;
import gtp.bytebites.restaurant.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface RestaurantService {
    RestaurantDto saveRestaurant(Restaurant restaurant);
    Page<RestaurantSummaryDto> getAllRestaurants(Pageable pageable);
    RestaurantDto getRestaurantById(UUID restaurantId);
    List<MenuItem> getRestaurantMenu(UUID restaurantId);
    MenuItem addMenuItemToRestaurant(UUID restaurantId, MenuItem menuItem);
}
