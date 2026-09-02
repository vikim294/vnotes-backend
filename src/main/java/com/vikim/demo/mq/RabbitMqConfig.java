package com.vikim.demo.mq;

import com.vikim.demo.outbox.OutboxEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RabbitMqConfig {

    private final OutboxEventMapper outboxEventMapper;

    // Spring Boot 4.1 的 rabbit template 定制（customize）入口
    @Bean
    public RabbitTemplateCustomizer rabbitTemplateCustomizer() {
        return rabbitTemplate -> {
            // 事件成功发送到 mq 到回调
            rabbitTemplate.setConfirmCallback(
                    (correlationData, ack, cause) -> {
                        if (correlationData == null) {
                            log.error("RabbitMQ publish confirm callback: correlationData is null");
                            return;
                        }

                        String eventId = correlationData.getId();

                        // ack 且没被 return 才能标记为 sent
                        if (ack && correlationData.getReturned() == null) {
                            log.info("RabbitMQ publish confirmed, eventId={}", eventId);

                            // 更新 outbox_events 状态为 sent
                            outboxEventMapper.markSent(eventId);
                        } else {
                            // nack 或 return
                            log.error("RabbitMQ publish failed, eventId={}, ack={}, cause={}", eventId, ack, cause);
                        }
                    }
            );

            // 让事件在无法路由时被退回
            rabbitTemplate.setMandatory(true);
            // 事件退回时的回调
            rabbitTemplate.setReturnsCallback(
                    returned -> {
                        log.error("RabbitMQ publish returned, exchange={}, routingKey={}, replyCode={}, replyText={}",
                                returned.getExchange(),
                                returned.getRoutingKey(),
                                returned.getReplyCode(),
                                returned.getReplyText()
                        );
                    }
            );
        };
    }

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
