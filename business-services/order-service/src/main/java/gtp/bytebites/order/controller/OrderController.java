package gtp.bytebites.order.controller;

import gtp.bytebites.order.dto.request.PlaceOrderRequest;
import gtp.bytebites.order.dto.response.OrderDto;
import gtp.bytebites.order.service.OrderService;
import gtp.bytebites.util.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderDto>> placeOrder(@Valid @RequestBody PlaceOrderRequest request, Principal principal) {
        String customerId = principal.getName();
        ApiResponse<OrderDto> response = orderService.placeOrder(request, customerId);

        if (response.isSuccess()) {
            OrderDto newOrder = response.getData();
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(newOrder.id())
                    .toUri();
            return ResponseEntity.created(location).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(@PathVariable UUID orderId) {
        OrderDto orderDto = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success(orderDto));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getMyOrders(Principal principal) {
        String customerId = principal.getName();
        List<OrderDto> orders = orderService.getOrdersForCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
}