package gtp.bytebites.restaurant.service;

import gtp.bytebites.restaurant.dto.response.RestaurantDto;
import gtp.bytebites.restaurant.dto.response.RestaurantSummaryDto;
import gtp.bytebites.restaurant.exception.ResourceNotFoundException;
import gtp.bytebites.restaurant.mapper.RestaurantMapper;
import gtp.bytebites.restaurant.model.MenuItem;
import gtp.bytebites.restaurant.model.Restaurant;
import gtp.bytebites.restaurant.repository.MenuItemRepository;
import gtp.bytebites.restaurant.repository.RestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantMapper restaurantMapper;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository,
                                 MenuItemRepository menuItemRepository, RestaurantMapper restaurantMapper) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.restaurantMapper = restaurantMapper;
    }

    @Override
    public RestaurantDto saveRestaurant(Restaurant restaurant) {
        return restaurantMapper.toDto(restaurantRepository.save(restaurant));
    }

    @Override
    public Page<RestaurantSummaryDto> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findAll(pageable)
                .map(restaurantMapper::toSummaryDto);
    }

    @Override
    public RestaurantDto getRestaurantById(UUID restaurantId) {
        return restaurantMapper.toDto(restaurantRepository.getRestaurantById(restaurantId));
    }

    @Override
    public List<MenuItem> getRestaurantMenu(UUID restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    @Override
    @Transactional
    public MenuItem addMenuItemToRestaurant(UUID restaurantId, MenuItem menuItem) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        menuItem.setRestaurant(restaurant);
        return menuItemRepository.save(menuItem);
    }
}
