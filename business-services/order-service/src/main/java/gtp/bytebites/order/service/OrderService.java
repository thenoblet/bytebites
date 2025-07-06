package gtp.bytebites.order.service;

import gtp.bytebites.order.dto.request.PlaceOrderRequest;
import gtp.bytebites.order.dto.response.OrderDto;
import gtp.bytebites.util.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    ApiResponse<OrderDto> placeOrder(PlaceOrderRequest request, String customerId);
    OrderDto getOrderById(UUID orderId);
    List<OrderDto> getOrdersForCustomer(String customerId);
    Page<OrderDto> getOrdersForRestaurant(UUID restaurantId, String ownerId, Pageable pageable);
}