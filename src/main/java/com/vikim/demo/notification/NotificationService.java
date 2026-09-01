package com.vikim.demo.notification;

import com.vikim.demo.processedmessage.ProcessedMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final ProcessedMessageMapper processedMessageMapper;

    public List<FetchUserNotificationsResponse> fetchUserNotifications(Long userId){
        return notificationMapper.fetchUserNotifications(userId);
    }

    @Transactional
    public void createNotificationForUser(Long userId, NotificationType type, String title, String content, UUID eventId, String consumer){
        // insert processed event
        processedMessageMapper.createProcessedMessage(eventId.toString(), consumer);

        // insert notification
        notificationMapper.createNotificationForUser(type, userId, title, content);
    }
}
