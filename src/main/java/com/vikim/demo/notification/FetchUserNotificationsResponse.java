package com.vikim.demo.notification;

import java.time.Instant;

public record FetchUserNotificationsResponse(
        Long id,
        NotificationType  type,
        String title,
        String content,
        boolean hasRead,
        Instant createdAt
) {
}
