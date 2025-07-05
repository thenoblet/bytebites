package gtp.bytebites.order.mapper;

import gtp.bytebites.order.dto.request.PlaceOrderRequest;
import gtp.bytebites.order.dto.response.OrderDto;
import gtp.bytebites.order.dto.response.OrderItemDto;
import gtp.bytebites.order.event.OrderPlacedEvent;
import gtp.bytebites.order.model.Order;
import gtp.bytebites.order.model.OrderItem;
import gtp.bytebites.order.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    /**
     * Converts the incoming API request (DTO) into a database entity.
     * This entity is not yet saved and is missing runtime data like customerId and totalPrice.
     *
     * @param request The request from the controller.
     *
     * @return An Order entity ready to be enriched and saved.
     */
    public Order toEntity(PlaceOrderRequest request) {
        Order order = Order.builder()
                .restaurantId(request.restaurantId())
                .status(OrderStatus.PENDING) // Orders always start as PENDING.
                .build();

        request.items().stream()
                .map(this::toOrderItemEntity)
                .forEach(order::addOrderItem);

        return order;
    }

    /**
     * Converts a database Order entity into a DTO suitable for sending back in an API response.
     *
     * @param order The saved Order entity from the database.
     *
     * @return An OrderDto for the API client.
     */
    public OrderDto toDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getRestaurantId(),
                order.getStatus().name(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(this::toOrderItemDto)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Converts a saved Order entity into an event payload for RabbitMQ.
     *
     * @param order The saved Order entity from the database.
     *
     * @return An OrderPlacedEvent record to be published.
     */
    public OrderPlacedEvent toOrderPlacedEvent(Order order) {
        return new OrderPlacedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getRestaurantId(),
                order.getTotalPrice(),
                order.getItems().stream()
                        .map(this::toOrderItemData)
                        .collect(Collectors.toList())
        );
    }


    private OrderItem toOrderItemEntity(PlaceOrderRequest.OrderItemRequest itemRequest) {
        return OrderItem.builder()
                .menuItemId(itemRequest.menuItemId())
                .menuItemName(itemRequest.menuItemName())
                .quantity(itemRequest.quantity())
                .price(itemRequest.price())
                .build();
    }

    private OrderItemDto toOrderItemDto(OrderItem orderItem) {
        return new OrderItemDto(
                orderItem.getMenuItemId(),
                orderItem.getMenuItemName(),
                orderItem.getQuantity(),
                orderItem.getPrice()
        );
    }

    /**
     * Private helper to map an OrderItem entity to the event's item format.
     */
    private OrderPlacedEvent.OrderItemData toOrderItemData(OrderItem orderItem) {
        return new OrderPlacedEvent.OrderItemData(
                orderItem.getMenuItemId(),
                orderItem.getMenuItemName(),
                orderItem.getQuantity()
        );
    }

    /**
     * Converts a list of Order entities into a list of OrderDto objects.
     *
     * @param orders A list of Order entities from the database.
     * @return A list of OrderDto objects for the API client.
     */
    public List<OrderDto> toDtos(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        return orders.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

}


