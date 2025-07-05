package gtp.bytebites.auth.service;

import gtp.bytebites.auth.dto.response.UserResponse;
import gtp.bytebites.auth.dto.response.UserSummaryResponse;
import gtp.bytebites.auth.mapper.UserMapper;
import gtp.bytebites.auth.model.User;
import gtp.bytebites.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository,   UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Cacheable("users")
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Page<UserSummaryResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toSummaryResponse);
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public boolean existsById(UUID userId) {
        return userRepository.existsById(userId);
    }
}
