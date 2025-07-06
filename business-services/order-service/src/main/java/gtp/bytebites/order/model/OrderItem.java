package gtp.bytebites.order.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID menuItemId;

    private String menuItemName;

    private Integer quantity;

    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderItem() {}

    private OrderItem(OrderItemBuilder builder) {
        this.id = builder.id;
        this.menuItemId = builder.menuItemId;
        this.menuItemName = builder.menuItemName;
        this.quantity = builder.quantity;
        this.price = builder.price;
        this.order = builder.order;
    }

    public static OrderItemBuilder builder() {
        return new OrderItemBuilder();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(UUID menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public static class OrderItemBuilder {
        private UUID id;
        private UUID menuItemId;
        private String menuItemName;
        private Integer quantity;
        private BigDecimal price;
        private Order order;

        public OrderItemBuilder id(UUID id) { this.id = id; return this; }
        public OrderItemBuilder menuItemId(UUID menuItemId) { this.menuItemId = menuItemId; return this; }
        public OrderItemBuilder menuItemName(String menuItemName) { this.menuItemName = menuItemName; return this; }
        public OrderItemBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public OrderItemBuilder price(BigDecimal price) { this.price = price; return this; }
        public OrderItemBuilder order(Order order) { this.order = order; return this; }

        public OrderItem build() {
            return new OrderItem(this);
        }
    }
}