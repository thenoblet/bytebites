package gtp.bytebites.restaurant.controller;

import gtp.bytebites.restaurant.dto.request.CreateMenuRequest;
import gtp.bytebites.restaurant.dto.response.MenuDto;
import gtp.bytebites.restaurant.service.MenuService;
import gtp.bytebites.util.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{id}/menu")
public class MenuController {
    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @validationUtil.isRestaurantOwner(#id)")
    public ResponseEntity<ApiResponse<MenuDto>> createMenuItem(
            @PathVariable UUID id,
            @Valid @RequestBody CreateMenuRequest request) {

        MenuDto createdMenu = menuService.createMenuItem(id, request);
        ApiResponse<MenuDto> response = ApiResponse.success(createdMenu, "Menu item created successfully.");

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdMenu.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
