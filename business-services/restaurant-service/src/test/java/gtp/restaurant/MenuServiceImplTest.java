package gtp.restaurant;

import gtp.bytebites.restaurant.dto.request.CreateMenuRequest;
import gtp.bytebites.restaurant.dto.response.MenuDto;
import gtp.bytebites.util.exception.ResourceNotFoundException;
import gtp.bytebites.restaurant.model.MenuItem;
import gtp.bytebites.restaurant.model.Restaurant;
import gtp.bytebites.restaurant.service.MenuServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MenuServiceImplTest extends BaseServiceTest {

    @InjectMocks
    private MenuServiceImpl menuService;

    @Test
    void createMenuItem_shouldCreateAndReturnDtoWhenRestaurantExists() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = createTestRestaurant();
        restaurant.setId(restaurantId);

        CreateMenuRequest request = new CreateMenuRequest(
                "Pizza Margherita",
                "Classic pizza with tomato and mozzarella",
                BigDecimal.valueOf(12.99)
        );

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.name());
        menuItem.setDescription(request.description());
        menuItem.setPrice(request.price());
        menuItem.setRestaurant(restaurant);

        MenuItem savedItem = createTestMenuItem(restaurantId);
        savedItem.setId(UUID.randomUUID());

        MenuDto expectedDto = new MenuDto(
                savedItem.getId(),
                savedItem.getName(),
                savedItem.getDescription(),
                savedItem.getPrice(),
                restaurantId,
                savedItem.getCreatedAt(),
                savedItem.getUpdatedAt()
        );

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(savedItem);
        when(menuMapper.toDto(savedItem)).thenReturn(expectedDto);

        // When
        MenuDto result = menuService.createMenuItem(restaurantId, request);

        // Then
        assertNotNull(result);
        assertEquals(savedItem.getId(), result.id());
        assertEquals(request.name(), result.name());
        assertEquals(request.description(), result.description());
        assertEquals(request.price(), result.price());
        assertEquals(restaurantId, result.restaurantId());

        verify(restaurantRepository).findById(restaurantId);
        verify(menuItemRepository).save(any(MenuItem.class));
        verify(menuMapper).toDto(savedItem);
    }

    @Test
    void createMenuItem_shouldThrowWhenRestaurantNotFound() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        CreateMenuRequest request = new CreateMenuRequest(
                "Pizza Margherita",
                "Classic pizza with tomato and mozzarella",
                BigDecimal.valueOf(12.99)
        );

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () ->
                menuService.createMenuItem(restaurantId, request));

        verify(restaurantRepository).findById(restaurantId);
        verify(menuItemRepository, never()).save(any());
        verify(menuMapper, never()).toDto(any());
    }

    // Add to MenuServiceImplTest

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "1.00", "9999.99"})
    void createMenuItem_withValidPriceBoundaries_shouldSucceed(String priceStr) {
        // Given
        BigDecimal price = new BigDecimal(priceStr);
        UUID restaurantId = UUID.randomUUID();
        CreateMenuRequest request = new CreateMenuRequest(
                "Boundary Test Item",
                "Test description",
                price
        );

        Restaurant restaurant = createTestRestaurant();
        MenuItem savedItem = createTestMenuItem(restaurantId);
        savedItem.setPrice(price);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.save(any())).thenReturn(savedItem);
        when(menuMapper.toDto(any())).thenReturn(new MenuDto(
                savedItem.getId(),
                savedItem.getName(),
                savedItem.getDescription(),
                savedItem.getPrice(),
                restaurantId,
                null,
                null
        ));

        // When
        MenuDto result = menuService.createMenuItem(restaurantId, request);

        // Then
        assertEquals(price, result.price());
    }

}
