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
        System.out.println("   POST /api/tasks   - создать задачу (требует API Key)");
        System.out.println("   GET  /api/tasks   - все задачи (требует API Key)");
        System.out.println("   GET  /health      - проверка здоровья (публичный)");
        System.out.println("   GET  /status      - статус сервиса (публичный)");
    }
}