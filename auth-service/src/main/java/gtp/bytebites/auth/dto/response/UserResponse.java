package gtp.bytebites.auth.dto.response;

import gtp.bytebites.auth.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String name,
        User.Role role,
        boolean isOAuthUser,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public UserResponse(User user) {
        this(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isOauth2User(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}