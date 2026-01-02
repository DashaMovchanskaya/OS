package org.example.controller;

import org.example.model.Task;
import org.example.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Домашняя страница API
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "To-Do List API with SQLite");
        response.put("version", "1.0");
        response.put("database", "SQLite (todo.db file)");
        response.put("totalTasks", taskService.getTotalTaskCount());
        return ResponseEntity.ok(response);
    }

    // Статистика
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", taskService.getTotalTaskCount());
        stats.put("todoCount", taskService.getTasksByStatus("todo").size());
        stats.put("inProgressCount", taskService.getTasksByStatus("in_progress").size());
        stats.put("doneCount", taskService.getTasksByStatus("done").size());
        return ResponseEntity.ok(stats);
    }

    // Поиск по заголовку
    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchTasks(@RequestParam String q) {
        System.out.println("Search tasks with query: " + q);
        List<Task> tasks = taskService.searchTasksByTitle(q);
        return ResponseEntity.ok(tasks);
    }

    // GET /tasks - Получить все задачи
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        System.out.println("GET /tasks requested");
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    // GET /tasks/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        System.out.println("GET /tasks/" + id + " requested");
        try {
            Task task = taskService.getTaskById(id);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // POST /tasks
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        System.out.println("POST /tasks requested with: " + task.getTitle());

        // Валидация
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Title is required");
            return ResponseEntity.badRequest().body(error);
        }

        Task createdTask = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    // PUT /tasks/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        System.out.println("PUT /tasks/" + id + " requested");

        try {
            // Валидация
            if (updatedTask.getTitle() == null || updatedTask.getTitle().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Title is required");
                return ResponseEntity.badRequest().body(error);
            }

            Task task = taskService.updateTask(id, updatedTask);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // PATCH /tasks/{id} для обновления статуса
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        System.out.println("PATCH /tasks/" + id + " requested");

        try {
            String newStatus = request.get("status");
            if (newStatus == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Status field is required");
                return ResponseEntity.badRequest().body(error);
            }

            // Валидация статуса
            if (!isValidStatus(newStatus)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status. Use: todo, in_progress, done");
                return ResponseEntity.badRequest().body(error);
            }

            Task updatedTask = taskService.updateTaskStatus(id, newStatus);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // DELETE /tasks/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        System.out.println("DELETE /tasks/" + id + " requested");

        try {
            taskService.deleteTask(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // GET /tasks/status/{status} - фильтр по статусу
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getTasksByStatus(@PathVariable String status) {
        System.out.println("GET /tasks/status/" + status + " requested");

        if (!isValidStatus(status)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid status. Use: todo, in_progress, done");
            return ResponseEntity.badRequest().body(error);
        }

        List<Task> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }

    // Вспомогательные методы
    private boolean isValidStatus(String status) {
        return status.equals("todo") ||
                status.equals("in_progress") ||
                status.equals("done");
    }
}