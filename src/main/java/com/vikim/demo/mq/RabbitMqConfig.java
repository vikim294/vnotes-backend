package com.vikim.demo.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    // 消息类型的序列化、反序列化
    // RabbitTemplate：Java 对象 → JSON
    // @RabbitListener：JSON → Java 对象
    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // exchange

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(
                RabbitMqConstants.USER_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(
                        RabbitMqConstants.DEAD_LETTER_EXCHANGE
                )
                .durable(true)
                .build();
    }

    // queue

    @Bean
    public Queue notificationUserRegisteredQueue() {
        return QueueBuilder
                .durable(
                        RabbitMqConstants.NOTIFICATION_USER_REGISTERED_QUEUE
                )
                .deadLetterExchange(
                        RabbitMqConstants.DEAD_LETTER_EXCHANGE
                )
                .deadLetterRoutingKey(
                        RabbitMqConstants.NOTIFICATION_USER_REGISTERED_FAILED_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public Queue emailUserRegisteredQueue() {
        return QueueBuilder
                .durable(
                        RabbitMqConstants.EMAIL_USER_REGISTERED_QUEUE
                )
                .deadLetterExchange(
                        RabbitMqConstants.DEAD_LETTER_EXCHANGE
                )
                .deadLetterRoutingKey(
                        RabbitMqConstants.EMAIL_USER_REGISTERED_FAILED_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public Queue emailUserDeadLetterQueue() {
        return QueueBuilder
                .durable(
                        RabbitMqConstants.EMAIL_USER_DLQ
                )
                .build();
    }

    @Bean
    public Queue notificationUserDeadLetterQueue() {
        return QueueBuilder
                .durable(
                        RabbitMqConstants.NOTIFICATION_USER_DLQ
                )
                .build();
    }

    // binding

    @Bean
    public Binding userRegisteredNotificationBinding(
            DirectExchange userExchange,
            Queue notificationUserRegisteredQueue
    ) {
        return BindingBuilder
                .bind(notificationUserRegisteredQueue)
                .to(userExchange)
                .with(
                        RabbitMqConstants.USER_REGISTERED_ROUTING_KEY
                );
    }

    @Bean
    public Binding userRegisteredEmailBinding(
            DirectExchange userExchange,
            Queue emailUserRegisteredQueue
    ) {
        return BindingBuilder
                .bind(emailUserRegisteredQueue)
                .to(userExchange)
                .with(
                        RabbitMqConstants.USER_REGISTERED_ROUTING_KEY
                );
    }

    @Bean
    public Binding emailUserDeadLetterBinding(
            DirectExchange deadLetterExchange,
            Queue emailUserDeadLetterQueue
    ) {
        return BindingBuilder
                .bind(emailUserDeadLetterQueue)
                .to(deadLetterExchange)
                .with(
                        RabbitMqConstants.EMAIL_USER_REGISTERED_FAILED_ROUTING_KEY
                );
    }

    @Bean
    public Binding notificationUserDeadLetterBinding(
            DirectExchange deadLetterExchange,
            Queue notificationUserDeadLetterQueue
    ) {
        return BindingBuilder
                .bind(notificationUserDeadLetterQueue)
                .to(deadLetterExchange)
                .with(
                        RabbitMqConstants.NOTIFICATION_USER_REGISTERED_FAILED_ROUTING_KEY
                );
    }


}
