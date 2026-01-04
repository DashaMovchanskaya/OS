package org.example.controller;

import org.example.dto.ApiResponse;
import org.example.service.MessageQueueService;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final MessageQueueService messageQueueService;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;

    @Autowired
    public QueueController(
            MessageQueueService messageQueueService,
            RabbitTemplate rabbitTemplate,
            RabbitAdmin rabbitAdmin) {
        this.messageQueueService = messageQueueService;
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = rabbitAdmin;
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> healthCheck() {
        return Mono.fromCallable(() -> {
            Map<String, Object> response = new HashMap<>();

            try {
                rabbitTemplate.convertAndSend("", "amq.direct", "ping");

                response.put("status", "UP");
                response.put("message", "RabbitMQ подключен и работает");
                response.put("queueStatus", messageQueueService.getQueueStatus());
                response.put("host", rabbitTemplate.getConnectionFactory().getHost());
                response.put("port", rabbitTemplate.getConnectionFactory().getPort());

                ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>success(response);
                return ResponseEntity.ok(apiResponse);
            } catch (Exception e) {
                response.put("status", "DOWN");
                response.put("message", "RabbitMQ недоступен: " + e.getMessage());
                response.put("error", e.getClass().getSimpleName());

                ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>error("RabbitMQ недоступен", e.getMessage())
                        .withMetadata(response);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(apiResponse);
            }
        });
    }

    @PostMapping("/test-message")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> sendTestMessage(@RequestBody Map<String, String> request) {
        return Mono.fromCallable(() -> {
            String message = request.get("message");

            if (message == null || message.trim().isEmpty()) {
                // Создаем Map для ошибки
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("error", "Message is required");
                errorData.put("timestamp", System.currentTimeMillis());

                ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>success(errorData);
                response.setSuccess(false);
                response.setError("Validation error");
                return ResponseEntity.badRequest().body(response);
            }

            try {
                messageQueueService.sendNotification("Тестовое сообщение: " + message);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Тестовое сообщение отправлено");
                result.put("content", message);

                ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>success(result);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("details", e.getMessage());
                errorDetails.put("exception", e.getClass().getName());

                ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>error("Ошибка отправки сообщения")
                        .withMetadata(errorDetails);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        });
    }

    @GetMapping("/stats")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getQueueStats() {
        return Mono.fromCallable(() -> {
            Map<String, Object> stats = new HashMap<>();

            try {
                String[] queueNames = {
                        "task.created.queue",
                        "task.updated.queue",
                        "task.deleted.queue",
                        "task.notification.queue",
                        "task.dead.letter.queue"
                };

                Map<String, Object> queueInfo = new HashMap<>();
                for (String queueName : queueNames) {
                    try {
                        org.springframework.amqp.core.Queue queue = new org.springframework.amqp.core.Queue(queueName);
                        Map<String, Object> queueData = new HashMap<>();
                        queueData.put("name", queueName);
                        queueData.put("durable", queue.isDurable());
                        queueData.put("exclusive", queue.isExclusive());
                        queueData.put("autoDelete", queue.isAutoDelete());
                        queueInfo.put(queueName, queueData);
                    } catch (Exception e) {
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("error", "Не удалось получить информацию");
                        errorData.put("reason", e.getMessage());
                        queueInfo.put(queueName, errorData);
                    }
                }

                stats.put("queues", queueInfo);
                stats.put("totalQueues", queueNames.length);
                stats.put("status", "active");
                stats.put("rabbitmqVersion", "3.12+");
                stats.put("managementEnabled", true);
                stats.put("connectionFactory", rabbitTemplate.getConnectionFactory().getClass().getSimpleName());

                ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>success(stats);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("basicInfo", messageQueueService.getQueueStatus());
                errorData.put("error", e.getMessage());

                ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>error("Не удалось получить полную статистику")
                        .withMetadata(errorData);
                return ResponseEntity.ok(response);
            }
        });
    }

    @GetMapping("/test-send")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> testSend() {
        return Mono.fromCallable(() -> {
            try {
                String testMessage = "Test message from API at " + System.currentTimeMillis();
                rabbitTemplate.convertAndSend("task.exchange", "task.created", Map.of(
                        "test", true,
                        "message", testMessage,
                        "timestamp", System.currentTimeMillis()
                ));

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Тестовое сообщение отправлено");
                result.put("exchange", "task.exchange");
                result.put("routingKey", "task.created");

                ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>success(result);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("details", e.getMessage());
                errorDetails.put("cause", e.getCause() != null ? e.getCause().getMessage() : "N/A");

                ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>error("Ошибка отправки тестового сообщения")
                        .withMetadata(errorDetails);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        });
    }

    @GetMapping("/config")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getRabbitConfig() {
        return Mono.fromCallable(() -> {
            Map<String, Object> config = new HashMap<>();

            try {
                config.put("host", rabbitTemplate.getConnectionFactory().getHost());
                config.put("port", rabbitTemplate.getConnectionFactory().getPort());
                config.put("virtualHost", rabbitTemplate.getConnectionFactory().getVirtualHost());
                config.put("username", rabbitTemplate.getConnectionFactory().getUsername());

                config.put("exchangeNames", new String[]{
                        "task.exchange",
                        "notification.exchange",
                        "dead.letter.exchange"
                });

                config.put("queueNames", new String[]{
                        "task.created.queue",
                        "task.updated.queue",
                        "task.deleted.queue",
                        "task.notification.queue",
                        "task.dead.letter.queue"
                });

                config.put("routingKeys", new String[]{
                        "task.created",
                        "task.updated",
                        "task.deleted"
                });

                ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>success(config);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("details", e.getMessage());

                ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>error("Ошибка получения конфигурации")
                        .withMetadata(errorDetails);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        });
    }

    @DeleteMapping("/purge")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> purgeAllQueues() {
        return Mono.fromCallable(() -> {
            Map<String, Object> result = new HashMap<>();
            result.put("message", "В демо-режиме очистка очередей не выполняется");
            result.put("warning", "В продакшене эта операция удалит все сообщения из очередей");
            result.put("availableOperations", new String[]{
                    "GET /api/queue/health - проверка состояния",
                    "GET /api/queue/stats - статистика",
                    "POST /api/queue/test-message - тестовое сообщение"
            });
            result.put("timestamp", System.currentTimeMillis());

            ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>success(result);
            return ResponseEntity.ok(response);
        });
    }
}