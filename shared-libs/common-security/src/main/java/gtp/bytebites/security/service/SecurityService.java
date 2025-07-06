package gtp.bytebites.security.service;

import gtp.bytebites.security.util.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityService {
    private final AuthenticationFacade authenticationFacade;
    private final OAuth2ResourceServerProperties.Jwt jwtProperties;

    private static final String USER_ID_CLAIM = "userId";
    private static final String ROLES_CLAIM = "roles";
    private static final String DEFAULT_PRINCIPAL_CLAIM = "sub";

    public UUID getCurrentUserId() {
        Authentication authentication = authenticationFacade.getAuthentication();
        return extractUserIdFromAuthentication(authentication);
    }

    public void validateOwnershipOrAdmin(UUID ownerId) {
        UUID currentUserId = getCurrentUserId();
        boolean isAdmin = isAdmin();

        if (!currentUserId.equals(ownerId) && !isAdmin) {
            throw new AccessDeniedException("You don't have permission to perform this action");
        }
    }

    private UUID extractUserIdFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            try {
                String userId = jwt.getClaim(USER_ID_CLAIM);
                if (userId != null) {
                    return UUID.fromString(userId);
                }

                String principalClaim = jwtProperties.getPrincipalClaimName() != null
                        ? jwtProperties.getPrincipalClaimName()
                        : DEFAULT_PRINCIPAL_CLAIM;

                userId = jwt.getClaim(principalClaim);
                if (userId == null) {
                    throw new IllegalStateException(
                            "JWT token doesn't contain required claims. Tried: " +
                                    USER_ID_CLAIM + " and " + principalClaim);
                }
                return UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid UUID format in user ID claim", e);
            }
        }
        throw new IllegalStateException("Unsupported authentication type - expected JWT");
    }

    private boolean isAdmin() {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            boolean hasAdminAuthority = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (hasAdminAuthority) return true;

            List<String> roles = jwt.getClaim(ROLES_CLAIM);
            return roles != null && roles.contains("ROLE_ADMIN");
        }
        return false;
    }
}