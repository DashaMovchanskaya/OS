package org.example.service;

import org.example.model.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final List<Task> tasks = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<Task> getAllTasks() {
        return tasks.stream()
                .map(task -> task.toShortVersion())
                .collect(Collectors.toList());
    }

    public Task getTaskById(Long id) {
        return tasks.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Задача не найдена с id: " + id));
    }

    public Task createTask(Task task) {
        task.setId(idCounter.getAndIncrement());
        tasks.add(task);
        return task;
    }

    // Обновить задачу полностью
    public Task updateTask(Long id, Task updatedTask) {
        Task existingTask = getTaskById(id);

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());

        return existingTask;
    }

    public Task updateTaskStatus(Long id, String newStatus) {
        Task existingTask = getTaskById(id);
        existingTask.setStatus(newStatus);
        return existingTask;
    }

    public void deleteTask(Long id) {
        Task taskToDelete = getTaskById(id);
        tasks.remove(taskToDelete);
    }

    public void initializeSampleData() {
        createTask(new Task("Изучить Spring Boot",
                "Пройти курс по созданию REST API",
                "in_progress"));

        createTask(new Task("Купить продукты",
                "Молоко, хлеб, яйца",
                "todo"));

        createTask(new Task("Закончить проект",
                "Доделать To-Do List API",
                "done"));
    }
}