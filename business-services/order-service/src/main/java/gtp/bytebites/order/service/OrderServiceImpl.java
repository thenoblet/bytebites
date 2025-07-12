package gtp.bytebites.order.service;

import gtp.bytebites.events.event.OrderPlacedEvent;
import gtp.bytebites.order.client.RestaurantServiceClient;
import gtp.bytebites.order.client.dto.RestaurantDto;
import gtp.bytebites.order.dto.request.PlaceOrderRequest;
import gtp.bytebites.order.dto.response.OrderDto;
import gtp.bytebites.order.event.OrderEventPublisher;
import gtp.bytebites.order.mapper.OrderMapper;
import gtp.bytebites.order.model.Order;
import gtp.bytebites.order.model.OrderItem;
import gtp.bytebites.order.model.OrderStatus;
import gtp.bytebites.order.repository.OrderRepository;
import gtp.bytebites.util.dto.ApiResponse;
import gtp.bytebites.util.exception.OrderNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher orderEventPublisher; // CHANGED: Injected publisher
    private final RestaurantServiceClient restaurantServiceClient;

    @Autowired
    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderMapper orderMapper,
            OrderEventPublisher orderEventPublisher, // CHANGED: No more RabbitTemplate here
            RestaurantServiceClient restaurantServiceClient) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.orderEventPublisher = orderEventPublisher;
        this.restaurantServiceClient = restaurantServiceClient;
    }

    @Override
    @Transactional
    public ApiResponse<OrderDto> placeOrder(PlaceOrderRequest request, String customerId) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setRestaurantId(request.restaurantId());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> items = request.items().stream()
                .map(itemRequest -> {
                    OrderItem item = new OrderItem();
                    item.setMenuItemId(itemRequest.menuItemId());
                    item.setQuantity(itemRequest.quantity());
                    item.setMenuItemName(itemRequest.menuItemName());
                    item.setPrice(itemRequest.price());
                    item.setOrder(order);
                    return item;
                }).collect(Collectors.toList());
        order.setItems(items);

        BigDecimal totalPrice = items.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);

        OrderPlacedEvent event = orderMapper.toOrderPlacedEvent(savedOrder);
        orderEventPublisher.publishOrderPlaced(event);

        OrderDto orderDto = orderMapper.toDto(savedOrder);
        return ApiResponse.success(orderDto, "Order placed successfully.");
    }

    @Override
    public OrderDto getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toDto(order);
    }

    @Override
    public List<OrderDto> getOrdersForCustomer(String customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orderMapper.toDtos(orders);
    }

    @Override
    public Page<OrderDto> getOrdersForRestaurant(UUID restaurantId, String ownerId, Pageable pageable) {
        RestaurantDto restaurant = restaurantServiceClient.getRestaurantById(restaurantId);

        if (restaurant == null || !restaurant.owner().equals(ownerId)) {
            throw new AccessDeniedException("You do not have permission to view orders for this restaurant.");
        }

        Page<Order> orders = orderRepository.findByRestaurantId(restaurantId, pageable);
        return orders.map(orderMapper::toDto);
    }
}
