package org.example;

import org.example.model.Task;
import org.example.service.MessageQueueService;
import org.example.config.RabbitMQConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageQueueServiceTest {

    private MessageQueueService messageQueueService;
    private DummyRabbitTemplate dummyRabbitTemplate;

    static class DummyRabbitTemplate extends RabbitTemplate {
        static class Call {
            final String exchange;
            final String routingKey;
            final Object message;
            Call(String e, String r, Object m) {
                this.exchange = e;
                this.routingKey = r;
                this.message = m;
            }
        }
        final List<Call> calls = new ArrayList<>();

        @Override
        public void convertAndSend(String exchange, String routingKey, Object message) {
            calls.add(new Call(exchange, routingKey, message));
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        dummyRabbitTemplate = new DummyRabbitTemplate();
        messageQueueService = new MessageQueueService();

        Field field = MessageQueueService.class.getDeclaredField("rabbitTemplate");
        field.setAccessible(true);
        field.set(messageQueueService, dummyRabbitTemplate);
    }

    @Test
    void testSendTaskCreatedEvent() {
        Task task = new Task("Title", "Desc", "todo");
        task.setId(1L);

        messageQueueService.sendTaskCreatedEvent(task, "tester");

        assertEquals(2, dummyRabbitTemplate.calls.size());

        DummyRabbitTemplate.Call eventCall = dummyRabbitTemplate.calls.get(0);
        assertEquals(RabbitMQConfig.TASK_EXCHANGE, eventCall.exchange);
        assertEquals(RabbitMQConfig.TASK_CREATED_ROUTING_KEY, eventCall.routingKey);

        DummyRabbitTemplate.Call notifCall = dummyRabbitTemplate.calls.get(1);
        assertEquals(RabbitMQConfig.NOTIFICATION_EXCHANGE, notifCall.exchange);
        assertEquals("", notifCall.routingKey);
    }

    @Test
    void testSendTaskUpdatedEvent() {
        Task oldTask = new Task("Old", "Desc", "todo");
        oldTask.setId(1L);
        Task newTask = new Task("New", "Desc2", "done");
        newTask.setId(1L);

        messageQueueService.sendTaskUpdatedEvent(oldTask, newTask, "tester");

        assertEquals(2, dummyRabbitTemplate.calls.size());
        assertEquals(RabbitMQConfig.TASK_EXCHANGE, dummyRabbitTemplate.calls.get(0).exchange);
        assertEquals(RabbitMQConfig.TASK_UPDATED_ROUTING_KEY, dummyRabbitTemplate.calls.get(0).routingKey);
        assertEquals(RabbitMQConfig.NOTIFICATION_EXCHANGE, dummyRabbitTemplate.calls.get(1).exchange);
    }

    @Test
    void testSendTaskDeletedEvent() {
        messageQueueService.sendTaskDeletedEvent(1L, "Title", "todo", "tester");

        assertEquals(2, dummyRabbitTemplate.calls.size());
        assertEquals(RabbitMQConfig.TASK_EXCHANGE, dummyRabbitTemplate.calls.get(0).exchange);
        assertEquals(RabbitMQConfig.TASK_DELETED_ROUTING_KEY, dummyRabbitTemplate.calls.get(0).routingKey);
        assertEquals(RabbitMQConfig.NOTIFICATION_EXCHANGE, dummyRabbitTemplate.calls.get(1).exchange);
    }

    @Test
    void testSendNotification() {
        messageQueueService.sendNotification("Hello");

        assertEquals(1, dummyRabbitTemplate.calls.size());
        DummyRabbitTemplate.Call call = dummyRabbitTemplate.calls.get(0);
        assertEquals(RabbitMQConfig.NOTIFICATION_EXCHANGE, call.exchange);
        assertEquals("", call.routingKey);
        assertEquals("Hello", call.message);
    }

    @Test
    void testGetQueueStatus() {
        String status = messageQueueService.getQueueStatus();
        assertTrue(status.contains("RabbitMQ подключен"));
        assertTrue(status.contains(RabbitMQConfig.TASK_CREATED_QUEUE));
        assertTrue(status.contains(RabbitMQConfig.TASK_UPDATED_QUEUE));
        assertTrue(status.contains(RabbitMQConfig.TASK_DELETED_QUEUE));
    }
}

