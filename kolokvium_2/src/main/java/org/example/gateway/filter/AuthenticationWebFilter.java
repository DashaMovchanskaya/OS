package org.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.List;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;


@Component
public class AuthenticationWebFilter implements WebFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationWebFilter.class);
    private static final List<String> VALID_API_KEYS = List.of("admin-123-token", "user-456-token", "test-789-token", "my-secret-api-key");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        logger.info("[Web Filter] {} {} - проверка доступа", method, path);

        if (isPublicPath(path)) {
            logger.info("Публичный путь {} - доступ разрешен", path);
            return chain.filter(exchange);
        }

        if (path.startsWith("/api/")) {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return unauthorizedResponse(exchange, "API Key is required. Use X-API-Key header.");
            }
            if (!VALID_API_KEYS.contains(apiKey)) {
                return unauthorizedResponse(exchange, "Invalid API Key.");
            }
            logger.info("Доступ разрешен для API Key '{}' на путь {}", apiKey, path);
            return chain.filter(exchange.mutate()
                    .request(builder -> builder
                            .header("X-User-Authenticated", "true")
                            .header("X-User-Role", getUserRole(apiKey))
                            .header("X-User-ID", "user-" + apiKey.hashCode()))
                    .build());
        }

        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path) {
        boolean isPublic = path.equals("/") ||
                path.equals("/health") ||
                path.equals("/status") ||
                path.startsWith("/public/") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs");

        logger.debug("Проверка пути {}: публичный = {}", path, isPublic);
        return isPublic;
    }

    private boolean isValidApiKey(String apiKey) {
        boolean valid = VALID_API_KEYS.contains(apiKey);
        logger.debug("Проверка API Key '{}': valid = {}", apiKey, valid);
        return valid;
    }

    private String getUserRole(String apiKey) {
        if (apiKey.contains("admin")) return "ADMIN";
        if (apiKey.contains("user")) return "USER";
        return "GUEST";
    }

    private Mono<Void> unauthorizedResponse(org.springframework.web.server.ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        exchange.getResponse().getHeaders().add("WWW-Authenticate", "API-Key");

        String responseBody = String.format(
                "{\"error\": \"Unauthorized\", \"message\": \"%s\", \"timestamp\": \"%s\", \"path\": \"%s\"}",
                message,
                java.time.LocalDateTime.now(),
                exchange.getRequest().getURI().getPath()
        );

        logger.error("Unauthorized: {} - {}", exchange.getRequest().getURI().getPath(), message);

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
