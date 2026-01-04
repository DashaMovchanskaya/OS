package org.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RateLimitLogger implements GatewayFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitLogger.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
        String client = apiKey != null ? apiKey : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        logger.info("[Gateway] {} {} from {} (API Key: {})",
                method, path, client, apiKey != null ? "present" : "missing");

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            logger.info("[Gateway] {} {} - Status: {}",
                    method, path, exchange.getResponse().getStatusCode());
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
