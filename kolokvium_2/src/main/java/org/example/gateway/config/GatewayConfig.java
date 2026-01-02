package org.example.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class GatewayConfig {


    @Bean
    @Primary
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-API-Key");

            if (apiKey != null && !apiKey.isEmpty()) {
                return Mono.just("api-key-" + apiKey);
            }

            // Если нет API Key, возвращаем "anonymous"
            return Mono.just("anonymous");
        };
    }

    // Rate Limiting по IP адресу
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(
                        exchange.getRequest().getRemoteAddress()
                ).getAddress().getHostAddress()
        );
    }

    // Rate Limiting по пользователю (комбинация API Key + IP)
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-API-Key");
            String ip = Objects.requireNonNull(
                    exchange.getRequest().getRemoteAddress()
            ).getAddress().getHostAddress();

            String key = apiKey != null ?
                    "user-" + apiKey + "-" + ip :
                    "anonymous-" + ip;

            return Mono.just(key);
        };
    }
}