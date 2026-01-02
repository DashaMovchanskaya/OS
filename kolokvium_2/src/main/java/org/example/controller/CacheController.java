package org.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    private CacheManager cacheManager;

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getCacheInfo() {
        Map<String, Object> info = new HashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                info.put(cacheName, "Redis cache");
            }
        });

        info.put("totalCaches", cacheManager.getCacheNames().size());
        info.put("cacheManager", cacheManager.getClass().getSimpleName());

        return ResponseEntity.ok(info);
    }

    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return ResponseEntity.ok(Map.of(
                    "message", "Cache cleared successfully",
                    "cacheName", cacheName
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "error", "Cache not found",
                "cacheName", cacheName
        ));
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });

        return ResponseEntity.ok(Map.of(
                "message", "All caches cleared successfully",
                "clearedCount", String.valueOf(cacheManager.getCacheNames().size())
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheNames", cacheManager.getCacheNames());
        stats.put("cacheCount", cacheManager.getCacheNames().size());
        stats.put("cacheType", "Redis");

        return ResponseEntity.ok(stats);
    }
}