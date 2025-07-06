package gtp.bytebites.order.client;

import gtp.bytebites.order.client.dto.RestaurantDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "restaurant-service")
public interface RestaurantServiceClient {

    @GetMapping("/api/v1/restaurants/{restaurantId}")
    RestaurantDto getRestaurantById(@PathVariable("restaurantId") UUID restaurantId);
}

