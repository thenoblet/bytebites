package gtp.bytebites.restaurant.util;

import gtp.bytebites.restaurant.repository.RestaurantRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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

        try {
            UUID userId = null;

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                Object userIdClaim = jwt.getClaim("userId");
                if (userIdClaim == null) {
                    return false;
                }

                try {
                    userId = UUID.fromString(userIdClaim.toString());
                } catch (IllegalArgumentException e) {
                    return false;
                }

            } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
                Object principal = authentication.getPrincipal();

                if (principal instanceof UserDetails) {
                    return false;
                } else if (principal instanceof String) {
                    try {
                        userId = UUID.fromString((String) principal);
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                } else {
                    return false;
                }

            } else {
                return false;
            }

            UUID finalUserId = userId;
            return restaurantRepository.findById(restaurantId)
                    .map(restaurant -> {
                        UUID ownerId = restaurant.getOwnerId();
                        return ownerId != null && ownerId.equals(finalUserId);
                    })
                    .orElse(false);

        } catch (Exception e) {
            return false;
        }
    }
}