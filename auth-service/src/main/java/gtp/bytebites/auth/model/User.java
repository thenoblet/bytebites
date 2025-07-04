package gtp.bytebites.auth.model;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false)
    private UUID id;

    @Size(min = 3, max = 60)
    private String name;

    @NotBlank
    @Column(unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean oauth2user = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public boolean isOauth2User() {
        return oauth2user;
    }

    public enum Role {
        ROLE_CUSTOMER,
        ROLE_RESTAURANT_OWNER,
        ROLE_ADMIN
    }

    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(String name, String email, String password) {
        this();
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User(String email, String password, Role role) {
        this();
        this.email = email;
        this.password = password;
        setRole(role);
    }

    public User(String name, String email, String password, Role role) {
        this();
        this.name = name;
        this.email = email;
        this.password = password;
        setRole(role);
    }

    @AssertTrue(message = "Password is required for non-OAuth users")
    private boolean isPasswordValid() {
        return oauth2user || (password != null && !password.isBlank());
    }


    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (!oauth2user && (password == null || password.trim().isEmpty())) {
            throw new IllegalArgumentException("Password is required for non-OAuth users");
        }
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isOauth2user() {
        return oauth2user;
    }

    public void setOauth2user(boolean oauth2user) {
        this.oauth2user = oauth2user;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return isOauth2user() == user.isOauth2user() && Objects.equals(getId(), user.getId()) && Objects.equals(getName(), user.getName()) && Objects.equals(getEmail(), user.getEmail()) && Objects.equals(getPassword(), user.getPassword()) && getRole() == user.getRole() && Objects.equals(getCreatedAt(), user.getCreatedAt()) && Objects.equals(getUpdatedAt(), user.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getEmail(), getPassword(), getRole(), isOauth2user(), getCreatedAt(), getUpdatedAt());
    }
}
