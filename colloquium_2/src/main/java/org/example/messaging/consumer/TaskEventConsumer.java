package org.example.messaging.consumer;

import org.example.config.RabbitMQConfig;
import org.example.messaging.dto.TaskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class TaskEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TaskEventConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.TASK_CREATED_QUEUE)
    public void handleTaskCreated(TaskEvent event) {
        logger.info("[Consumer] Получено событие создания задачи:");
        logger.info("   ID: {}", event.getTaskId());
        logger.info("   Название: {}", event.getTitle());
        logger.info("   Статус: {}", event.getNewStatus());
        logger.info("   Создано: {}", event.getPerformedBy());
        logger.info("   Время: {}", event.getTimestamp());
    }

    @RabbitListener(queues = RabbitMQConfig.TASK_UPDATED_QUEUE)
    public void handleTaskUpdated(TaskEvent event) {
        logger.info("[Consumer] Получено событие обновления задачи:");
        logger.info("   ID: {}", event.getTaskId());
        logger.info("   Название: {}", event.getTitle());
        logger.info("   Старый статус: {}", event.getOldStatus());
        logger.info("   Новый статус: {}", event.getNewStatus());
        logger.info("   Обновлено: {}", event.getPerformedBy());
    }

    @RabbitListener(queues = RabbitMQConfig.TASK_DELETED_QUEUE)
    public void handleTaskDeleted(TaskEvent event) {
        logger.info("[Consumer] Получено событие удаления задачи:");
        logger.info("   ID: {}", event.getTaskId());
        logger.info("   Название: {}", event.getTitle());
        logger.info("   Статус при удалении: {}", event.getOldStatus());
        logger.info("   Удалено: {}", event.getPerformedBy());
    }

    @RabbitListener(queues = RabbitMQConfig.TASK_NOTIFICATION_QUEUE)
    public void handleNotification(String message) {
        logger.info("[Notification] {}", message);
    }


    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE)
    public void handleDeadLetter(Object message) {
        logger.warn("[Dead Letter] Получено недоставленное сообщение:");
        logger.warn("   Тип: {}", message.getClass().getName());
        logger.warn("   Содержимое: {}", message.toString());
    }
}