package gtp.restaurant;

import gtp.bytebites.restaurant.dto.response.MenuDto;
import gtp.bytebites.restaurant.dto.response.RestaurantDto;
import gtp.bytebites.restaurant.dto.response.RestaurantSummaryDto;
import gtp.bytebites.restaurant.mapper.MenuMapper;
import gtp.bytebites.restaurant.mapper.RestaurantMapper;
import gtp.bytebites.restaurant.model.MenuItem;
import gtp.bytebites.restaurant.model.Restaurant;
import gtp.bytebites.restaurant.repository.MenuItemRepository;
import gtp.bytebites.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {

    @Mock
    protected RestaurantRepository restaurantRepository;

    @Mock
    protected MenuItemRepository menuItemRepository;

    @Mock
    protected RestaurantMapper restaurantMapper;

    @Mock
    protected MenuMapper menuMapper;

    protected Restaurant createTestRestaurant() {
        Restaurant restaurant = Restaurant.builder()
                .name("Test Restaurant")
                .address("50 Asaman St")
                .owner(UUID.randomUUID())
                .cuisineType("Italian")
                .build();
        restaurant.setId(UUID.randomUUID());
        return restaurant;
    }

    protected MenuItem createTestMenuItem(UUID restaurantId) {
        MenuItem item = new MenuItem();
        item.setId(UUID.randomUUID());
        item.setName("Pizza Margherita");
        item.setDescription("Classic pizza with tomato and mozzarella");
        item.setPrice(BigDecimal.valueOf(12.99));
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        item.setRestaurant(restaurant);
        return item;
    }

    protected MenuDto createTestMenuDto(UUID restaurantId) {
        return new MenuDto(
                UUID.randomUUID(),
                "Pizza Margherita",
                "Classic pizza with tomato and mozzarella",
                BigDecimal.valueOf(12.99),
                restaurantId,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    protected RestaurantDto createTestRestaurantDto(UUID id) {
        return new RestaurantDto(
                id,
                "Test Restaurant",
                "123 Test St",
                "A test restaurant",
                "Italian",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    protected RestaurantSummaryDto createTestRestaurantSummaryDto(UUID id) {
        return new RestaurantSummaryDto(
                id,
                "Test Restaurant",
                "123 Test St",
                "Italian"
        );
    }
}