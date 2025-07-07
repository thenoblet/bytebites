package gtp.bytebites.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(JwtGlobalFilter.class);

    private final ReactiveJwtDecoder jwtDecoder;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${app.security.whitelist}")
    private List<String> whitelist;

    public JwtGlobalFilter(ReactiveJwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (isWhitelisted(path)) {
            log.trace("Path is whitelisted, skipping JWT validation: {}", path);
            return chain.filter(exchange);
        }

        log.debug("Path is not whitelisted, checking for JWT: {}", path);
        String token = extractToken(request);

        if (token == null) {
            log.warn("Missing Authorization Bearer token for non-whitelisted path: {}", path);
            return unauthorized(exchange);
        }

        return this.jwtDecoder.decode(token)
                .flatMap(jwt -> {
                    ServerHttpRequest enrichedRequest = enrichRequest(request, jwt);
                    log.debug("Token validated successfully. User ID: {}, Roles: {}",
                            jwt.getClaim("userId"),
                            jwt.getClaimAsStringList("roles"));
                    return chain.filter(exchange.mutate().request(enrichedRequest).build());
                })
                .onErrorResume(e -> {
                    log.error("Invalid token: {}", e.getMessage());
                    return unauthorized(exchange);
                });
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isWhitelisted(String path) {
        return whitelist.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private ServerHttpRequest enrichRequest(ServerHttpRequest request, Jwt jwt) {
        // Get userId from custom claim (not from jwt.getId())
        String userId = jwt.getClaim("userId"); // This matches your token generation
        String email = jwt.getSubject();
        List<String> rolesList = jwt.getClaimAsStringList("roles");
        String rolesHeader = String.join(",", rolesList != null ? rolesList : List.of());

        return request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Email", email)
                .header("X-User-Roles", rolesHeader)
                .build();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
