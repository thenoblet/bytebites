package gtp.bytebites.restaurant.service;

import gtp.bytebites.restaurant.dto.request.CreateMenuRequest;
import gtp.bytebites.restaurant.dto.response.MenuDto;
import gtp.bytebites.restaurant.mapper.MenuMapper;
import gtp.bytebites.restaurant.model.MenuItem;
import gtp.bytebites.restaurant.model.Restaurant;
import gtp.bytebites.restaurant.repository.MenuItemRepository;
import gtp.bytebites.restaurant.repository.RestaurantRepository;
import gtp.bytebites.util.exception.ConflictException;
import gtp.bytebites.util.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MenuServiceImpl implements MenuService {
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuMapper menuMapper;

    @Autowired
    public MenuServiceImpl(RestaurantRepository restaurantRepository, MenuItemRepository menuItemRepository, MenuMapper menuMapper) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.menuMapper = menuMapper;
    }

    @Override
    @Transactional
    public MenuDto createMenuItem(UUID restaurantId, CreateMenuRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        menuItemRepository.findByRestaurant_IdAndName(restaurantId, request.name())
                .ifPresent(item -> {
                    throw new ConflictException(
                            String.format("Menu item with name '%s' already exists for this restaurant",
                                    request.name())
                    );
                });

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.name());
        menuItem.setDescription(request.description());
        menuItem.setPrice(request.price());
        menuItem.setRestaurant(restaurant);

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        return menuMapper.toDto(savedMenuItem);
    }
}