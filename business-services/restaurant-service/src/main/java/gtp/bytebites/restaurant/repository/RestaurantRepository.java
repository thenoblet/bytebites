package gtp.bytebites.restaurant.repository;

import gtp.bytebites.restaurant.model.Restaurant;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    Page<Restaurant> findAll(@NotNull Pageable pageable);
    Restaurant getRestaurantById(UUID restaurantId);
}
