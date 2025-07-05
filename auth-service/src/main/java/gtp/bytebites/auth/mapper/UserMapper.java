package gtp.bytebites.auth.mapper;

import gtp.bytebites.auth.dto.request.RegisterRequest;
import gtp.bytebites.auth.dto.response.UserResponse;
import gtp.bytebites.auth.dto.response.UserSummaryResponse;
import gtp.bytebites.auth.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request) {
        return new User(
                request.name(),
                request.email(),
                request.password()
        );
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isOauth2User(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public UserSummaryResponse toSummaryResponse(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
