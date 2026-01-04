package org.example.service;

import org.example.config.RabbitMQConfig;
import org.example.messaging.dto.TaskEvent;
import org.example.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageQueueService {

    private static final Logger logger = LoggerFactory.getLogger(MessageQueueService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendTaskCreatedEvent(Task task, String performedBy) {
        try {
            TaskEvent event = TaskEvent.created(
                    task.getId(),
                    task.getTitle(),
                    task.getStatus(),
                    performedBy
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TASK_EXCHANGE,
                    RabbitMQConfig.TASK_CREATED_ROUTING_KEY,
                    event
            );

            logger.info("Отправлено событие: Задача создана - {}", task.getId());
            sendNotification("Новая задача создана: " + task.getTitle());

        } catch (AmqpException e) {
            logger.error("Ошибка отправки события создания задачи: {}", e.getMessage(), e);
        }
    }

    public void sendTaskUpdatedEvent(Task oldTask, Task newTask, String performedBy) {
        try {
            TaskEvent event = TaskEvent.updated(
                    newTask.getId(),
                    newTask.getTitle(),
                    oldTask.getStatus(),
                    newTask.getStatus(),
                    performedBy
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TASK_EXCHANGE,
                    RabbitMQConfig.TASK_UPDATED_ROUTING_KEY,
                    event
            );

            logger.info("Отправлено событие: Задача обновлена - {}", newTask.getId());

            if (!oldTask.getStatus().equals(newTask.getStatus())) {
                sendNotification(
                        "Статус задачи обновлен: " + newTask.getTitle() +
                                " (" + oldTask.getStatus() + " → " + newTask.getStatus() + ")"
                );
            }

        } catch (AmqpException e) {
            logger.error("Ошибка отправки события обновления задачи: {}", e.getMessage(), e);
        }
    }

    public void sendTaskDeletedEvent(Long taskId, String title, String status, String performedBy) {
        try {
            TaskEvent event = TaskEvent.deleted(
                    taskId,
                    title,
                    status,
                    performedBy
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TASK_EXCHANGE,
                    RabbitMQConfig.TASK_DELETED_ROUTING_KEY,
                    event
            );

            logger.info("Отправлено событие: Задача удалена - {}", taskId);
            sendNotification("Задача удалена: " + title);

        } catch (AmqpException e) {
            logger.error("Ошибка отправки события удаления задачи: {}", e.getMessage(), e);
        }
    }

    public void sendNotification(String message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    "", // Fanout exchange не требует routing key
                    message
            );

            logger.info("Отправлено уведомление: {}", message);

        } catch (AmqpException e) {
            logger.error("Ошибка отправки уведомления: {}", e.getMessage(), e);
        }
    }

    public String getQueueStatus() {
        return "RabbitMQ подключен. Используются очереди: " +
                RabbitMQConfig.TASK_CREATED_QUEUE + ", " +
                RabbitMQConfig.TASK_UPDATED_QUEUE + ", " +
                RabbitMQConfig.TASK_DELETED_QUEUE;
    }
}