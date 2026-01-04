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
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

import java.util.List;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = {"tasks", "task", "stats"})
public class TaskService {
    private final TaskRepository taskRepository;
    private final MessageQueueService messageQueueService;
    private final Counter createCounter;
    private final Counter updateCounter;
    private final Counter deleteCounter;

    @Autowired
    public TaskService(TaskRepository taskRepository,
                       MessageQueueService messageQueueService,
                       MeterRegistry meterRegistry) {
        this.taskRepository = taskRepository;
        this.messageQueueService = messageQueueService;
        this.createCounter = Counter.builder("tasks.created").tag("type", "crud").register(meterRegistry);
        this.updateCounter = Counter.builder("tasks.updated").tag("type", "crud").register(meterRegistry);
        this.deleteCounter = Counter.builder("tasks.deleted").tag("type", "crud").register(meterRegistry);
    }

    @Cacheable(value = "tasks", key = "'all'")
    public List<Task> getAllTasks() {
        System.out.println("Загрузка всех задач из БД (кэш не найден)");
        return taskRepository.findAll();
    }

    @Cacheable(value = "task", key = "#id")
    public Task getTaskById(Long id) {
        System.out.println("Загрузка задачи " + id + " из БД (кэш не найден)");
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found: " + id));
    }

    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "'all'"),
            @CacheEvict(value = "stats", allEntries = true)
    })
    public Task createTask(Task task) {
        System.out.println("Инвалидация кэша при создании задачи");
        createCounter.increment();
        Task saved = taskRepository.save(task);

        messageQueueService.sendTaskCreatedEvent(saved, "system");

        return saved;
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

        Task oldTask = new Task(existing.getTitle(), existing.getDescription(), existing.getStatus());
        oldTask.setId(existing.getId());

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setStatus(updatedTask.getStatus());

        updateCounter.increment();
        Task saved = taskRepository.save(existing);

        messageQueueService.sendTaskUpdatedEvent(oldTask, saved, "system");

        return saved;
    }

    @Caching(evict = {
            @CacheEvict(value = "task", key = "#id"),
            @CacheEvict(value = "tasks", key = "'all'"),
            @CacheEvict(value = "stats", allEntries = true)
    })
    public void deleteTask(Long id) {
        System.out.println("Инвалидация кэша при удалении задачи");
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found: " + id));

        deleteCounter.increment();
        taskRepository.deleteById(id);

        messageQueueService.sendTaskDeletedEvent(existing.getId(), existing.getTitle(), existing.getStatus(), "system");
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
