package org.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final List<String> VALID_API_KEYS = List.of(
            "admin-123-token",
            "user-456-token",
            "test-789-token",
            "my-secret-api-key"
    );

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Публичные пути - пропускаем без проверки
            if (isPublicEndpoint(exchange)) {
                return chain.filter(exchange);
            }

            String apiKey = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-API-Key");

            if (apiKey == null || apiKey.trim().isEmpty()) {
                return unauthorizedResponse(exchange, "API Key is required");
            }

            if (!isValidApiKey(apiKey)) {
                return unauthorizedResponse(exchange, "Invalid API Key");
            }

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(builder -> builder
                            .header("X-User-Authenticated", "true")
                            .header("X-User-Role", getUserRole(apiKey))
                            .header("X-User-ID", getUserId(apiKey))
                    )
                    .build();

            return chain.filter(modifiedExchange);
        };
    }

    private boolean isPublicEndpoint(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        return path.startsWith("/health") ||
                path.startsWith("/status") ||
                path.startsWith("/public/") ||
                path.startsWith("/actuator/");
    }

    private boolean isValidApiKey(String apiKey) {
        return VALID_API_KEYS.contains(apiKey);
    }

    private String getUserRole(String apiKey) {
        if (apiKey.contains("admin")) return "ADMIN";
        if (apiKey.contains("user")) return "USER";
        return "GUEST";
    }

    private String getUserId(String apiKey) {
        return "user-" + apiKey.hashCode();
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        String responseBody = String.format(
                "{\"error\": \"Unauthorized\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
                message,
                java.time.LocalDateTime.now()
        );

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(responseBody.getBytes())));
    }

    public static class Config {
        private boolean enabled = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}