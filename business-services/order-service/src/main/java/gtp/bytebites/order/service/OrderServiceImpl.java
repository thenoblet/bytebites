package gtp.bytebites.order.service;

import gtp.bytebites.order.dto.request.PlaceOrderRequest;
import gtp.bytebites.order.dto.response.OrderDto;
import gtp.bytebites.order.event.OrderPlacedEvent;
import gtp.bytebites.order.mapper.OrderMapper;
import gtp.bytebites.order.model.Order;
import gtp.bytebites.order.model.OrderItem;
import gtp.bytebites.order.model.OrderStatus;
import gtp.bytebites.order.repository.OrderRepository;
import gtp.bytebites.util.dto.ApiResponse;
import gtp.bytebites.util.exception.OrderNotFoundException;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static gtp.bytebites.events.config.RabbitMQConfig.EXCHANGE_NAME;
import static gtp.bytebites.events.config.RabbitMQConfig.ROUTING_KEY;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.rabbitTemplate = rabbitTemplate;
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

        publishOrderPlacedEvent(savedOrder);

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

    private void publishOrderPlacedEvent(Order order) {
        List<OrderPlacedEvent.OrderItemData> itemData = order.getItems().stream()
                .map(item -> new OrderPlacedEvent.OrderItemData(
                        item.getMenuItemId(),
                        item.getMenuItemName(),
                        item.getQuantity()))
                .collect(Collectors.toList());

        OrderPlacedEvent event = new OrderPlacedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getRestaurantId(),
                order.getTotalPrice(),
                itemData
        );

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event);
    }
}