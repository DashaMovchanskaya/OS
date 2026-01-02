package org.example.repository;

import org.example.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Spring Data JPA автоматически предоставляет CRUD методы
    // Можно добавить кастомные методы при необходимости
}