package gtp.bytebites.restaurant.mapper;

import gtp.bytebites.restaurant.dto.response.MenuDto;
import gtp.bytebites.restaurant.model.MenuItem;
import gtp.bytebites.restaurant.model.Restaurant;
import org.springframework.stereotype.Component;

@Component
public class MenuMapper {

    public MenuDto toDto(MenuItem menuItem) {
        return new MenuDto(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getRestaurant() != null ? menuItem.getRestaurant().getId() : null,
                menuItem.getCreatedAt(),
                menuItem.getUpdatedAt()
        );
    }

    public MenuItem toEntity(MenuDto menuDto) {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(menuDto.id());
        menuItem.setName(menuDto.name());
        menuItem.setDescription(menuDto.description());
        menuItem.setPrice(menuDto.price());

        if (menuDto.restaurantId() != null) {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(menuDto.restaurantId());
            menuItem.setRestaurant(restaurant);
        }

        // createdAt and updatedAt will be handled by @PrePersist and @PreUpdate
        return menuItem;
    }
}
