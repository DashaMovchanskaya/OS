package org.example.controller;

import org.example.dto.ApiResponse;
import org.example.model.Task;
import org.example.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<Task>>>> getAllTasks() {
        return Mono.fromCallable(() -> {
            List<Task> tasks = taskService.getAllTasks();
            ApiResponse<List<Task>> response = ApiResponse.success(tasks);

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofSeconds(30)))
                    .header("X-Cache-Status", "HIT")
                    .body(response);
        });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Task>>> getTaskById(@PathVariable Long id) {
        return Mono.fromCallable(() -> {
            try {
                Task task = taskService.getTaskById(id);
                ApiResponse<Task> response = ApiResponse.success(task);

                return ResponseEntity.ok()
                        .cacheControl(CacheControl.maxAge(Duration.ofSeconds(60)))
                        .header("X-Cache-Status", "HIT")
                        .header("X-Cache-Key", "task:" + id)
                        .body(response);
            } catch (RuntimeException e) {
                ApiResponse<Task> response = ApiResponse.error("Task not found", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        });
    }

    @GetMapping("/stats")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getStats() {
        return Mono.fromCallable(() -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTasks", taskService.getTotalTaskCount());
            stats.put("timestamp", System.currentTimeMillis());
            stats.put("cacheEnabled", true);

            ApiResponse<Map<String, Object>> response = ApiResponse.success(stats);

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofSeconds(10)))
                    .body(response);
        });
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<Task>>> createTask(@RequestBody Task task) {
        return Mono.fromCallable(() -> {
            if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                ApiResponse<Task> response = ApiResponse.error("Title is required");
                return ResponseEntity.badRequest().body(response);
            }

            Task createdTask = taskService.createTask(task);
            ApiResponse<Task> response = ApiResponse.success(createdTask, "Task created successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Task>>> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        return Mono.fromCallable(() -> {
            try {
                if (updatedTask.getTitle() == null || updatedTask.getTitle().trim().isEmpty()) {
                    ApiResponse<Task> response = ApiResponse.error("Title is required");
                    return ResponseEntity.badRequest().body(response);
                }

                Task task = taskService.updateTask(id, updatedTask);
                ApiResponse<Task> response = ApiResponse.success(task, "Task updated successfully");

                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                ApiResponse<Task> response = ApiResponse.error("Task not found", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        });
    }

    @PatchMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Task>>> patchTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        return Mono.fromCallable(() -> {
            try {
                Task existing = taskService.getTaskById(id);

                if (updates.containsKey("title")) {
                    String title = (String) updates.get("title");
                    if (title == null || title.trim().isEmpty()) {
                        ApiResponse<Task> response = ApiResponse.error("Title is required");
                        return ResponseEntity.badRequest().body(response);
                    }
                    existing.setTitle(title);
                }

                if (updates.containsKey("description")) {
                    existing.setDescription((String) updates.get("description"));
                }

                if (updates.containsKey("status")) {
                    String status = (String) updates.get("status");
                    if (!List.of("todo", "in_progress", "done").contains(status)) {
                        ApiResponse<Task> response = ApiResponse.error("Invalid status value");
                        return ResponseEntity.badRequest().body(response);
                    }
                    existing.setStatus(status);
                }

                Task updated = taskService.updateTask(id, existing);
                ApiResponse<Task> response = ApiResponse.success(updated, "Task patched successfully");
                return ResponseEntity.ok(response);

            } catch (RuntimeException e) {
                ApiResponse<Task> response = ApiResponse.error("Task not found", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteTask(@PathVariable Long id) {
        return Mono.fromCallable(() -> {
            try {
                taskService.deleteTask(id);
                ApiResponse<Void> response = ApiResponse.success(null, "Task deleted successfully");

                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                ApiResponse<Void> response = ApiResponse.error("Task not found", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        });
    }
}