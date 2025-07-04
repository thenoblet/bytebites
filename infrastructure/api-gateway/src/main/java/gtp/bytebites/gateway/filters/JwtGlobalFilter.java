package gtp.bytebites.gateway.filters;

import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    private final ReactiveJwtDecoder jwtDecoder;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${app.security.whitelist}")
    private String whitelistPaths;

    public JwtGlobalFilter(ReactiveJwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("Authorization"))
                .switchIfEmpty(Mono.error(new JwtException("Missing Authorization header")))
                .flatMap(authHeader -> {
                    if (!authHeader.startsWith("Bearer ")) {
                        return Mono.error(new JwtException("Invalid Authorization header"));
                    }

                    String token = authHeader.substring(7);
                    return jwtDecoder.decode(token)
                            .doOnSuccess(jwt -> {
                                exchange.getRequest().mutate()
                                        .header("X-User-Id", jwt.getSubject())
                                        .header("X-User-Roles", String.join(",", jwt.getClaimAsStringList("roles")))
                                        .build();
                            });
                })
                .then(chain.filter(exchange))
                .onErrorResume(JwtException.class, e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                })
                .onErrorResume(Exception.class, e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return exchange.getResponse().setComplete();
                });
    }

    private boolean isWhitelisted(String path) {
        if (whitelistPaths == null || whitelistPaths.trim().isEmpty()) {
            return false;
        }

        List<String> patterns = Arrays.asList(whitelistPaths.split(","));
        return patterns.stream()
                .map(String::trim)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}