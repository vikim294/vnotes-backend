package com.vikim.vnotes.mq;

import com.vikim.vnotes.mq.events.UserRegisteredEvent;
import com.vikim.vnotes.notification.NotificationService;
import com.vikim.vnotes.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageConsumer {
    private final NotificationService notificationService;

    // 消费 NOTIFICATION_USER_REGISTERED_QUEUE 队列里的消息
    @RabbitListener(
            queues = RabbitMqConstants.NOTIFICATION_USER_REGISTERED_QUEUE
    )
    public void consumeNotificationUserRegistered(UserRegisteredEvent event)  {

        String title = "注册成功 🚀";
        String content = "欢迎使用 vnotes";
        String consumer = RabbitMqConstants.NOTIFICATION_USER_REGISTERED_CONSUMER;

        log.info(
                "发站内信：注册成功, eventId={}，consumer={}",
                event.eventId(),
                consumer
        );

        try {
            notificationService.createNotificationForUser(
                    event.userId(),
                    NotificationType.USER_REGISTERED_WELCOME,
                    title,
                    content,
                    event.eventId(),
                    consumer
            );
        } catch (DuplicateKeyException e) {
            // 吞掉异常就行
            log.info(
                    "发站内信跳过：重复消费, eventId={}，consumer={}",
                    event.eventId(),
                    consumer
            );
        }

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

        // 先不管 email
    }
}