package org.example.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicGatewayController {

    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "To-Do API Gateway");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        response.put("gateway", "Spring Cloud Gateway");
        return Mono.just(response);
    }

    @GetMapping("/status")
    public Mono<Map<String, Object>> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "operational");
        response.put("message", "Gateway is running");
        response.put("timestamp", LocalDateTime.now());
        response.put("uptime", Runtime.getRuntime().totalMemory() / (1024 * 1024) + " MB");
        return Mono.just(response);
    }

    @GetMapping("/info")
    public Mono<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "To-Do List API with Gateway");
        info.put("description", "CRUD API for tasks with message queues and caching");
        info.put("version", "1.0.0");
        info.put("gateway", "Spring Cloud Gateway (WebFlux)");
        info.put("endpoints", new HashMap<>() {{
            put("public", new String[]{"/health", "/status", "/public/info"});
            put("protected", new String[]{"/api/tasks/**", "/api/queue/**", "/api/cache/**"});
        }});
        info.put("authentication", new HashMap<>() {{
            put("header", "X-API-Key");
            put("valid_keys", new String[]{"admin-123-token", "user-456-token", "test-789-token"});
            put("note", "All /api/* endpoints require authentication");
        }});
        return Mono.just(info);
    }
}