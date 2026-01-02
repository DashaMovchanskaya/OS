package org.example.service;

import org.example.model.Task;
import org.example.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // Получить все задачи (без описания)
    public List<Task> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(Task::toShortVersion)
                .collect(Collectors.toList());
    }

    // Получить задачу по ID
    public Task getTaskById(Long id) {
        Optional<Task> task = taskRepository.findById(id);
        return task.orElseThrow(() ->
                new RuntimeException("Задача не найдена с id: " + id));
    }

    // Создать новую задачу
    @Transactional
    public Task createTask(Task task) {
        // Устанавливаем статус по умолчанию, если не указан
        if (task.getStatus() == null) {
            task.setStatus("todo");
        }

        return taskRepository.save(task);
    }

    // Обновить задачу полностью
    @Transactional
    public Task updateTask(Long id, Task updatedTask) {
        Task existingTask = getTaskById(id);

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());

        return taskRepository.save(existingTask);
    }

    // Обновить только статус задачи
    @Transactional
    public Task updateTaskStatus(Long id, String newStatus) {
        Task existingTask = getTaskById(id);
        existingTask.setStatus(newStatus);
        return taskRepository.save(existingTask);
    }

    // Удалить задачу
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Задача не найдена с id: " + id);
        }
        taskRepository.deleteById(id);
    }

    // Дополнительные методы
    public List<Task> getTasksByStatus(String status) {
        return taskRepository.findAll()
                .stream()
                .filter(task -> task.getStatus().equals(status))
                .map(Task::toShortVersion)
                .collect(Collectors.toList());
    }

    public long getTotalTaskCount() {
        return taskRepository.count();
    }

    // Поиск по ключевым словам в заголовке
    public List<Task> searchTasksByTitle(String keyword) {
        return taskRepository.findAll()
                .stream()
                .filter(task -> task.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .map(Task::toShortVersion)
                .collect(Collectors.toList());
    }

    // Инициализировать тестовые данные
    @PostConstruct
    @Transactional
    public void initializeSampleData() {
        if (taskRepository.count() == 0) {
            System.out.println("Инициализация тестовых данных в SQLite...");

            taskRepository.save(new Task(
                    "Изучить Spring Boot",
                    "Пройти курс по созданию REST API",
                    "in_progress"
            ));

            taskRepository.save(new Task(
                    "Купить продукты",
                    "Молоко, хлеб, яйца, фрукты",
                    "todo"
            ));

            taskRepository.save(new Task(
                    "Закончить проект To-Do List",
                    "Доделать API с SQLite базой данных",
                    "done"
            ));

            taskRepository.save(new Task(
                    "Подготовить отчет",
                    "Ежеквартальный финансовый отчет",
                    "in_progress"
            ));

            System.out.println("Создано 4 тестовые задачи");
        } else {
            System.out.println("В базе уже есть " + taskRepository.count() + " задач");
        }
    }
}