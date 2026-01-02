package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TodoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);

        System.out.println("To-Do API with SQLite запущен!");
        System.out.println("База данных: todo.db (файл в корне проекта)");
        System.out.println("Порт: 8080");
    }
}