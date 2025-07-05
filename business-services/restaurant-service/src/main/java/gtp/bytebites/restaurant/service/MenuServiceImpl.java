package gtp.bytebites.restaurant.service;

import gtp.bytebites.restaurant.dto.request.CreateMenuRequest;
import gtp.bytebites.restaurant.dto.response.MenuDto;
import gtp.bytebites.restaurant.model.MenuItem;
import gtp.bytebites.restaurant.model.Restaurant;
import gtp.bytebites.restaurant.repository.MenuItemRepository;
import gtp.bytebites.restaurant.repository.RestaurantRepository;
import gtp.bytebites.util.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MenuServiceImpl implements MenuService {
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    @Autowired
    public MenuServiceImpl(RestaurantRepository restaurantRepository, MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    @Transactional
    public MenuDto createMenuItem(UUID restaurantId, CreateMenuRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.name());
        menuItem.setDescription(request.description());
        menuItem.setPrice(request.price());
        menuItem.setRestaurant(restaurant);

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        return new MenuDto(
                savedMenuItem.getId(),
                savedMenuItem.getName(),
                savedMenuItem.getDescription(),
                savedMenuItem.getPrice(),
                savedMenuItem.getRestaurant().getId(),
                savedMenuItem.getCreatedAt(),
                savedMenuItem.getUpdatedAt()
        );
    }
}