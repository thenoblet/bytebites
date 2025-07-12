package gtp.restaurant;

import gtp.bytebites.restaurant.model.Restaurant;
import gtp.bytebites.restaurant.util.ValidationUtil;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidationUtilTest extends BaseServiceTest {

    @InjectMocks
    private ValidationUtil validationUtil;

    @Test
    void isRestaurantOwner_shouldReturnTrueForJwtWithMatchingOwnerId() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Restaurant restaurant = new Restaurant();
        restaurant.setOwnerId(ownerId);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn(ownerId.toString());

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt, Collections.emptyList(), "test");

        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When
        boolean result = validationUtil.isRestaurantOwner(restaurantId);

        // Then
        assertTrue(result);
        verify(restaurantRepository).findById(restaurantId);
    }

    @Test
    void isRestaurantOwner_shouldReturnFalseForJwtWithNonMatchingOwnerId() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();

        Restaurant restaurant = new Restaurant();
        restaurant.setOwnerId(ownerId);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn(differentUserId.toString());

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt, Collections.emptyList(), "test");

        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When
        boolean result = validationUtil.isRestaurantOwner(restaurantId);

        // Then
        assertFalse(result);
        verify(restaurantRepository).findById(restaurantId);
    }

    @Test
    void isRestaurantOwner_shouldReturnFalseForNonJwtAuthentication() {
        // Given
        UUID restaurantId = UUID.randomUUID();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", null, Collections.emptyList());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // When
        boolean result = validationUtil.isRestaurantOwner(restaurantId);

        // Then
        assertFalse(result);
        verify(restaurantRepository, never()).findById(any());
    }

    @Test
    void isRestaurantOwner_shouldReturnFalseWhenRestaurantNotFound() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn(ownerId.toString());

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt, Collections.emptyList(), "test");

        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // When
        boolean result = validationUtil.isRestaurantOwner(restaurantId);

        // Then
        assertFalse(result);
        verify(restaurantRepository).findById(restaurantId);
    }
}
