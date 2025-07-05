package gtp.bytebites.restaurant.service;

import gtp.bytebites.restaurant.dto.request.CreateMenuRequest;
import gtp.bytebites.restaurant.dto.response.MenuDto;

import java.util.UUID;

public interface MenuService {
    MenuDto createMenuItem(UUID restaurantId, CreateMenuRequest request);
}

