package org.example.controller;

import org.example.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    private CacheManager cacheManager;

    @GetMapping("/info")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getCacheInfo() {
        return Mono.fromCallable(() -> {
            Map<String, Object> info = new HashMap<>();

            cacheManager.getCacheNames().forEach(cacheName -> {
                org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    info.put(cacheName, Map.of(
                            "type", "Redis cache",
                            "nativeCache", cache.getNativeCache().getClass().getSimpleName()
                    ));
                }
            });

            Map<String, Object> data = new HashMap<>();
            data.put("caches", info);
            data.put("totalCaches", cacheManager.getCacheNames().size());
            data.put("cacheManager", cacheManager.getClass().getSimpleName());

            ApiResponse<Map<String, Object>> response = ApiResponse.success(data);

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofSeconds(30)))
                    .header("X-Cache-Status", "HIT")
                    .body(response);
        });
    }

    @DeleteMapping("/{cacheName}")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> clearCache(@PathVariable String cacheName) {
        return Mono.fromCallable(() -> {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);

            if (cache != null) {
                cache.clear();

                Map<String, String> result = Map.of(
                        "message", "Cache cleared successfully",
                        "cacheName", cacheName
                );

                ApiResponse<Map<String, String>> response = ApiResponse.success(result);
                return ResponseEntity.ok(response);
            }

            ApiResponse<Map<String, String>> response = ApiResponse.error("Cache not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        });
    }

    @DeleteMapping("/clear-all")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> clearAllCaches() {
        return Mono.fromCallable(() -> {
            int clearedCount = 0;

            for (String cacheName : cacheManager.getCacheNames()) {
                org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    clearedCount++;
                }
            }

            Map<String, String> result = Map.of(
                    "message", "All caches cleared successfully",
                    "clearedCount", String.valueOf(clearedCount),
                    "totalCaches", String.valueOf(cacheManager.getCacheNames().size())
            );

            ApiResponse<Map<String, String>> response = ApiResponse.success(result);
            return ResponseEntity.ok(response);
        });
    }

    @GetMapping("/stats")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getCacheStats() {
        return Mono.fromCallable(() -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("cacheNames", cacheManager.getCacheNames());
            stats.put("cacheCount", cacheManager.getCacheNames().size());
            stats.put("cacheType", "Redis");
            stats.put("timestamp", System.currentTimeMillis());

            ApiResponse<Map<String, Object>> response = ApiResponse.success(stats);

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofSeconds(10)))
                    .body(response);
        });
    }
}