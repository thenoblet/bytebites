package gtp.bytebites.restaurant.service;

import gtp.bytebites.restaurant.dto.response.MenuDto;
import gtp.bytebites.restaurant.dto.response.RestaurantDto;
import gtp.bytebites.restaurant.dto.response.RestaurantSummaryDto;
import gtp.bytebites.restaurant.exception.ResourceNotFoundException;
import gtp.bytebites.restaurant.mapper.MenuMapper;
import gtp.bytebites.restaurant.mapper.RestaurantMapper;
import gtp.bytebites.restaurant.model.MenuItem;
import gtp.bytebites.restaurant.model.Restaurant;
import gtp.bytebites.restaurant.repository.MenuItemRepository;
import gtp.bytebites.restaurant.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final MenuMapper menuItemMapper;
    @Autowired
    public RestaurantServiceImpl(RestaurantRepository restaurantRepository,
                                 MenuItemRepository menuItemRepository,
                                 RestaurantMapper restaurantMapper,
                                 MenuMapper menuItemMapper) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.restaurantMapper = restaurantMapper;
        this.menuItemMapper = menuItemMapper;
    }

    @Transactional
    @Override
    public RestaurantDto saveRestaurant(Restaurant restaurant) {
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return restaurantMapper.toDto(savedRestaurant);
    }

    @Override
    public Page<RestaurantSummaryDto> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findAll(pageable)
                .map(restaurantMapper::toSummaryDto);
    }

    @Override
    public RestaurantDto getRestaurantById(UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        return restaurantMapper.toDto(restaurant);
    }

    @Override
    public List<MenuItem> getRestaurantMenu(UUID restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    @Override
    public MenuItem addMenuItemToRestaurant(UUID restaurantId, MenuItem menuItem) {
        return null;
    }

    @Transactional
    @Override
    public MenuItem addMenuItemToRestaurant(UUID restaurantId, MenuDto menuItemDto) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        MenuItem menuItem = menuItemMapper.toEntity(menuItemDto);
        menuItem.setRestaurant(restaurant);
        return menuItemRepository.save(menuItem);
    }
}