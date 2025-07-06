package gtp.bytebites.restaurant.util;

import gtp.bytebites.restaurant.model.Restaurant;
import gtp.bytebites.restaurant.repository.RestaurantRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("validationUtil")
public class ValidationUtil {

    private final RestaurantRepository restaurantRepository;

    @Autowired
    public ValidationUtil(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Checks if the currently authenticated user is the owner of the specified restaurant.
     *
     * @param restaurantId The ID of the restaurant to check.
     * @return true if the user is the owner, false otherwise.
     */
    public boolean isRestaurantOwner(UUID restaurantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaim("userId"));


        return restaurantRepository.findById(restaurantId)
                .map(Restaurant::getOwner)
                .map(owner -> owner.equals(userId))
                .orElse(false);
    }
}
