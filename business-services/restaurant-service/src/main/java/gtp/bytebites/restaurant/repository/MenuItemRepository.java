package gtp.bytebites.restaurant.repository;

import gtp.bytebites.restaurant.model.MenuItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findByRestaurantId(UUID restaurantId);

    Optional<MenuItem> findByRestaurant_IdAndName(UUID restaurantId, String name);

    Optional<MenuItem> findByName(String burger);
}
