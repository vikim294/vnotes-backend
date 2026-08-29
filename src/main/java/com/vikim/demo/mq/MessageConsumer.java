package com.vikim.demo.mq;

import com.vikim.demo.mq.events.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageConsumer {

    // 消费 NOTIFICATION_USER_REGISTERED_QUEUE 队列里的消息
    @RabbitListener(
            queues = RabbitMqConstants.NOTIFICATION_USER_REGISTERED_QUEUE
    )
    public void consumeNotificationUserRegistered(UserRegisteredEvent event)  {

        log.info(
                "发站内信, userId={}",
                event.userId()
        );

    }

    // 消费 EMAIL_USER_REGISTERED_QUEUE 队列里的消息
    @RabbitListener(
            queues = RabbitMqConstants.EMAIL_USER_REGISTERED_QUEUE
    )
    public void consumeEmailUserRegistered(UserRegisteredEvent event)  {

        log.info(
                "发邮件, userId={}",
                event.userId()
        );

    }
}