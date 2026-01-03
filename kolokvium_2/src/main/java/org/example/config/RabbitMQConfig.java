package org.example.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TASK_CREATED_QUEUE = "task.created.queue";
    public static final String TASK_UPDATED_QUEUE = "task.updated.queue";
    public static final String TASK_DELETED_QUEUE = "task.deleted.queue";
    public static final String TASK_NOTIFICATION_QUEUE = "task.notification.queue";
    public static final String DEAD_LETTER_QUEUE = "task.dead.letter.queue";

    public static final String TASK_EXCHANGE = "task.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "dead.letter.exchange";

    public static final String TASK_CREATED_ROUTING_KEY = "task.created";
    public static final String TASK_UPDATED_ROUTING_KEY = "task.updated";
    public static final String TASK_DELETED_ROUTING_KEY = "task.deleted";

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("Сообщение доставлено в exchange");
            } else {
                System.out.println("Ошибка доставки: " + cause);
            }
        });

        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(TASK_EXCHANGE);
    }

    @Bean
    public FanoutExchange notificationExchange() {
        return new FanoutExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue taskCreatedQueue() {
        return QueueBuilder.durable(TASK_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_QUEUE)
                .withArgument("x-max-length", 10000)
                .build();
    }

    @Bean
    public Queue taskUpdatedQueue() {
        return QueueBuilder.durable(TASK_UPDATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Queue taskDeletedQueue() {
        return QueueBuilder.durable(TASK_DELETED_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(TASK_NOTIFICATION_QUEUE)
                .withArgument("x-message-ttl", 60000)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Binding taskCreatedBinding() {
        return BindingBuilder
                .bind(taskCreatedQueue())
                .to(taskExchange())
                .with(TASK_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding taskUpdatedBinding() {
        return BindingBuilder
                .bind(taskUpdatedQueue())
                .to(taskExchange())
                .with(TASK_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding taskDeletedBinding() {
        return BindingBuilder
                .bind(taskDeletedQueue())
                .to(taskExchange())
                .with(TASK_DELETED_ROUTING_KEY);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange());
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_QUEUE);
    }
}