package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TodoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
        System.out.println("To-Do API with Gateway запущен!");
        System.out.println("Порт: 8080");
        System.out.println("Эндпоинты:");
        System.out.println("   GET /api/tasks - список всех задач \n" +
                "GET /api/tasks/{id} - задача по ID \n" +
                "POST /api/tasks - создание задачи \n" +
                "PUT /api/tasks/{id} - обновление задачи \n" +
                "PATCH /api/tasks/{id} - частичное обновлеие задачи \n" +
                "DELETE /api/tasks/{id} → удаление задачи");
    }
}