package gtp.bytebites.restaurant.controller;

import gtp.bytebites.restaurant.dto.request.CreateRestaurantRequest;
import gtp.bytebites.restaurant.dto.response.RestaurantDto;
import gtp.bytebites.restaurant.dto.response.RestaurantSummaryDto;
import gtp.bytebites.restaurant.mapper.RestaurantMapper;
import gtp.bytebites.restaurant.service.RestaurantService;
import gtp.bytebites.util.dto.ApiResponse;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


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
    @PreAuthorize("hasAuthority('ROLE_RESTAURANT_OWNER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantDto>> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        restaurantService.saveRestaurant(restaurantMapper.toEntity(request)),
                        "Restaurant created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RestaurantSummaryDto>>> getAllRestaurants(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.success(restaurantService.getAllRestaurants(pageable)));
    }
}