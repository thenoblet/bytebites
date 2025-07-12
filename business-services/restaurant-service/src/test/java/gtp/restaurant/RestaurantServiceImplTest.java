package gtp.restaurant;

import gtp.bytebites.restaurant.dto.response.MenuDto;
import gtp.bytebites.restaurant.dto.response.RestaurantDto;
import gtp.bytebites.restaurant.dto.response.RestaurantSummaryDto;
import gtp.bytebites.restaurant.model.MenuItem;
import gtp.bytebites.restaurant.model.Restaurant;
import gtp.bytebites.restaurant.service.RestaurantServiceImpl;

import gtp.bytebites.util.exception.ConflictException;
import gtp.bytebites.util.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RestaurantServiceImpl}.
 *
 * <p>
 * This class tests the core business logic of the restaurant service,
 * ensuring correct behavior when saving restaurants, fetching lists,
 * handling missing data, and managing menu items.
 * </p>
 */
class RestaurantServiceImplTest extends BaseServiceTest {

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    /**
     * Tests saving a restaurant and verifying that the returned DTO
     * matches the saved entity.
     */
    @Test
    void saveRestaurant_shouldSaveAndReturnDto() {
        // Given
        Restaurant restaurant = createTestRestaurant();
        RestaurantDto expectedDto = createTestRestaurantDto(restaurant.getId());

        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);
        when(restaurantMapper.toDto(restaurant)).thenReturn(expectedDto);

        // When
        RestaurantDto result = restaurantService.saveRestaurant(restaurant);

        // Then
        assertNotNull(result);
        assertEquals(restaurant.getId(), result.id());
        verify(restaurantRepository).save(restaurant);
        verify(restaurantMapper).toDto(restaurant);
    }

    /**
     * Tests fetching a paginated list of restaurant summaries.
     */
    @Test
    void getAllRestaurants_shouldReturnPageOfSummaries() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Restaurant restaurant = createTestRestaurant();
        Page<Restaurant> restaurantPage = new PageImpl<>(Collections.singletonList(restaurant), pageable, 1);
        RestaurantSummaryDto summaryDto = createTestRestaurantSummaryDto(restaurant.getId());

        when(restaurantRepository.findAll(pageable)).thenReturn(restaurantPage);
        when(restaurantMapper.toSummaryDto(restaurant)).thenReturn(summaryDto);

        // When
        Page<RestaurantSummaryDto> result = restaurantService.getAllRestaurants(pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(summaryDto, result.getContent().getFirst());
        assertThat(result.getContent().getFirst().name()).isEqualTo(restaurant.getName());
        verify(restaurantRepository).findAll(pageable);
        verify(restaurantMapper).toSummaryDto(restaurant);
    }

    /**
     * Tests retrieving a restaurant by ID when it exists,
     * verifying that the returned DTO matches expectations.
     */
    @Test
    void getRestaurantById_shouldReturnDtoWhenFound() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = createTestRestaurant();
        restaurant.setId(restaurantId);
        RestaurantDto expectedDto = createTestRestaurantDto(restaurantId);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantMapper.toDto(restaurant)).thenReturn(expectedDto);

        // When
        RestaurantDto result = restaurantService.getRestaurantById(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(restaurantId, result.id());
        verify(restaurantRepository).findById(restaurantId);
        verify(restaurantMapper).toDto(restaurant);
    }

    /**
     * Tests that fetching a restaurant by ID throws a {@link ResourceNotFoundException}
     * when the restaurant does not exist.
     */
    @Test
    void getRestaurantById_shouldThrowWhenNotFound() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () ->
                restaurantService.getRestaurantById(restaurantId));
        verify(restaurantRepository).findById(restaurantId);
        verify(restaurantMapper, never()).toDto(any());
    }

    /**
     * Tests fetching all menu items for a given restaurant ID.
     */
    @Test
    void getRestaurantMenu_shouldReturnMenuItems() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        MenuItem item = createTestMenuItem(restaurantId);
        when(menuItemRepository.findByRestaurantId(restaurantId)).thenReturn(Collections.singletonList(item));

        // When
        List<MenuItem> result = restaurantService.getRestaurantMenu(restaurantId);

        // Then
        assertEquals(1, result.size());
        assertEquals(item, result.getFirst());
        verify(menuItemRepository).findByRestaurantId(restaurantId);
    }

    /**
     * Tests adding a menu item to a restaurant when the restaurant exists.
     */
    @Test
    void addMenuItemToRestaurant_shouldAddItemWhenRestaurantExists() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = createTestRestaurant();
        restaurant.setId(restaurantId);
        MenuDto menuDto = createTestMenuDto(restaurantId);
        MenuItem menuItem = createTestMenuItem(restaurantId);
        MenuItem savedItem = createTestMenuItem(restaurantId);
        savedItem.setId(UUID.randomUUID());

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(menuMapper.toEntity(menuDto)).thenReturn(menuItem);
        when(menuItemRepository.save(menuItem)).thenReturn(savedItem);

        // When
        MenuItem result = restaurantService.addMenuItemToRestaurant(restaurantId, menuDto);

        // Then
        assertNotNull(result);
        assertEquals(savedItem.getId(), result.getId());
        assertEquals(restaurantId, result.getRestaurant().getId());
        verify(restaurantRepository).findById(restaurantId);
        verify(menuMapper).toEntity(menuDto);
        verify(menuItemRepository).save(menuItem);
    }

    /**
     * Tests that adding a menu item throws a {@link ResourceNotFoundException}
     * if the target restaurant does not exist.
     */
    @Test
    void addMenuItemToRestaurant_shouldThrowWhenRestaurantNotFound() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        MenuDto menuDto = createTestMenuDto(restaurantId);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () ->
                restaurantService.addMenuItemToRestaurant(restaurantId, menuDto));
        verify(restaurantRepository).findById(restaurantId);
        verify(menuMapper, never()).toEntity(any());
        verify(menuItemRepository, never()).save(any());
    }

    @Test
    void saveRestaurant_withMinimumRequiredFields_shouldSucceed() {
        // Given
        Restaurant restaurant = Restaurant.builder()
                .name("Minimal Restaurant")
                .owner(UUID.randomUUID())
                .cuisineType("Generic")
                .build();

        when(restaurantRepository.save(any())).thenReturn(restaurant);
        when(restaurantMapper.toDto(any())).thenReturn(new RestaurantDto(
                restaurant.getId(),
                restaurant.getName(),
                null,
                null,
                restaurant.getCuisineType(),
                null,
                null
        ));

        // When
        RestaurantDto result = restaurantService.saveRestaurant(restaurant);

        // Then
        assertNotNull(result);
        assertEquals(restaurant.getName(), result.name());
        assertNull(result.address());
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void getAllRestaurants_withEmptyResult_shouldReturnEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(restaurantRepository.findAll(pageable))
                .thenReturn(Page.empty());

        // When
        Page<RestaurantSummaryDto> result = restaurantService.getAllRestaurants(pageable);

        // Then
        assertTrue(result.isEmpty());
        verify(restaurantRepository).findAll(pageable);
    }

    @Test
    void addMenuItemToRestaurant_withDuplicateName_shouldThrowConflictException() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        String duplicateName = "Duplicate Item";
        MenuDto menuDto = new MenuDto(
                null, duplicateName, "Description", BigDecimal.TEN, restaurantId, null, null
        );

        Restaurant restaurant = createTestRestaurant();
        MenuItem existingItem = createTestMenuItem(restaurantId);
        existingItem.setName(duplicateName);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findByRestaurant_IdAndName(restaurantId, duplicateName))
                .thenReturn(Optional.of(existingItem));

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> restaurantService.addMenuItemToRestaurant(restaurantId, menuDto));

        assertTrue(exception.getMessage().contains(duplicateName));
        verify(menuItemRepository, never()).save(any());
    }
}
