package gtp.bytebites.restaurant.controller;

import gtp.bytebites.restaurant.dto.request.CreateRestaurantRequest;
import gtp.bytebites.restaurant.dto.response.RestaurantDto;
import gtp.bytebites.restaurant.dto.response.RestaurantSummaryDto;
import gtp.bytebites.restaurant.mapper.RestaurantMapper;
import gtp.bytebites.restaurant.model.Restaurant;
import gtp.bytebites.restaurant.service.RestaurantService;
import gtp.bytebites.util.dto.ApiResponse;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final RestaurantMapper restaurantMapper;

    public RestaurantController(RestaurantService restaurantService,  RestaurantMapper restaurantMapper) {
        this.restaurantService = restaurantService;
        this.restaurantMapper = restaurantMapper;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RESTAURANT_OWNER', 'ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<RestaurantDto>> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request,
            Principal principal) {

        Restaurant restaurant = restaurantMapper.toEntity(request);
        RestaurantDto createdRestaurant = restaurantService.saveRestaurant(restaurant);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdRestaurant, "Restaurant created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RestaurantSummaryDto>>> getAllRestaurants(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<RestaurantSummaryDto> restaurants = restaurantService.getAllRestaurants(pageable);

        if (restaurants.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(restaurants, "No restaurants found"));
        }

        return ResponseEntity.ok(ApiResponse.success(restaurants));
    }
}