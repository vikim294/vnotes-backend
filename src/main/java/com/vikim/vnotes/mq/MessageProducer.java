package com.vikim.vnotes.mq;

import com.vikim.vnotes.mq.events.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConstants.USER_EXCHANGE,
                RabbitMqConstants.USER_REGISTERED_ROUTING_KEY,
                event,
                // Publisher Confirm ack 时，回调会收到该 eventId
                // 用于确认事件确实成功发给了 mq
                new CorrelationData(event.eventId())
        );
    }
}