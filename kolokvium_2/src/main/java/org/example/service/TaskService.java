package org.example.service;

import org.example.model.Task;
import org.example.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = {"tasks", "task", "stats"})
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Cacheable(value = "tasks", key = "'all'")
    public List<Task> getAllTasks() {
        System.out.println("Загрузка всех задач из БД (кэш не найден)");
        return taskRepository.findAll()
                .stream()
                .map(Task::toShortVersion)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "task", key = "#id")
    public Task getTaskById(Long id) {
        System.out.println("Загрузка задачи " + id + " из БД (кэш не найден)");
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found: " + id));
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "tasks", key = "'all'"),
                    @CacheEvict(value = "stats", allEntries = true)
            }
    )
    public Task createTask(Task task) {
        System.out.println("Инвалидация кэша при создании задачи");
        return taskRepository.save(task);
    }

    @Caching(
            put = @CachePut(value = "task", key = "#id"),
            evict = {
                    @CacheEvict(value = "tasks", key = "'all'"),
                    @CacheEvict(value = "stats", allEntries = true)
            }
    )
    public Task updateTask(Long id, Task updatedTask) {
        System.out.println("Обновление кэша задачи " + id);
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found: " + id));
        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setStatus(updatedTask.getStatus());
        return taskRepository.save(existing);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "task", key = "#id"),
                    @CacheEvict(value = "tasks", key = "'all'"),
                    @CacheEvict(value = "stats", allEntries = true)
            }
    )
    public void deleteTask(Long id) {
        System.out.println("🗑️ Инвалидация кэша при удалении задачи " + id);
        taskRepository.deleteById(id);
    }

    @Cacheable(value = "stats", key = "'taskCount'")
    public long getTotalTaskCount() {
        System.out.println("Расчет статистики из БД");
        return taskRepository.count();
    }

    @PostConstruct
    public void init() {
        if (taskRepository.count() == 0) {
            System.out.println("Инициализация тестовых данных...");
            taskRepository.save(new Task("Задача 1", "Описание 1", "todo"));
            taskRepository.save(new Task("Задача 2", "Описание 2", "in_progress"));
            taskRepository.save(new Task("Задача 3", "Описание 3", "done"));
        }
    }
}