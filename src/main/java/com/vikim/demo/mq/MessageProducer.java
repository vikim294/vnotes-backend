package com.vikim.demo.mq;

import com.vikim.demo.mq.events.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendUserRegisteredEvent(Long userId) {
        UUID eventId = UUID.randomUUID();

        UserRegisteredEvent event =
                new UserRegisteredEvent(
                        eventId,
                        userId
                );

        rabbitTemplate.convertAndSend(
                RabbitMqConstants.USER_EXCHANGE,
                RabbitMqConstants.USER_REGISTERED_ROUTING_KEY,
                event
        );
    }
}