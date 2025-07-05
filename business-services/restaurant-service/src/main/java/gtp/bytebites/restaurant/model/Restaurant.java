package gtp.bytebites.restaurant.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String address;

    private String description;

    @Column(name = "owner", nullable = false, updatable = false)
    private String owner;

    @Column(nullable = false)
    private String cuisineType;

    @OneToMany(
            mappedBy = "restaurant",
            cascade = CascadeType.ALL, // If we delete a restaurant, its menu items go too.
            orphanRemoval = true,
            fetch = FetchType.LAZY // Load menu items only when we explicitly ask for them.
    )
    private List<MenuItem> menuItems = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Restaurant(String name, String address, String owner, String cuisineType) {
        this.name = name;
        this.address = address;
        this.owner = owner;
        this.cuisineType = cuisineType;
    }

    public Restaurant() {
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

    public void addMenuItem(MenuItem menuItem) {
        menuItems.add(menuItem);
        menuItem.setRestaurant(this);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String address;
        private String owner;
        private String cuisineType;
        private List<MenuItem> menuItems = new ArrayList<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder cuisineType(String cuisineType) {
            this.cuisineType = cuisineType;
            return this;
        }

        public Builder menuItems(List<MenuItem> menuItems) {
            this.menuItems = menuItems;
            return this;
        }

        public Builder addMenuItem(MenuItem menuItem) {
            this.menuItems.add(menuItem);
            return this;
        }

        public Restaurant build() {
            Restaurant restaurant = new Restaurant(this.name, this.address, this.owner, this.cuisineType);
            restaurant.setMenuItems(this.menuItems != null ? this.menuItems : new ArrayList<>());
            return restaurant;
        }
    }
}
