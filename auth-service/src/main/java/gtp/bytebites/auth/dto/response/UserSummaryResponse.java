package gtp.bytebites.auth.dto.response;

import gtp.bytebites.auth.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String email,
        String name,
        User.Role role
) {
    public UserSummaryResponse(User user) {
        this(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
