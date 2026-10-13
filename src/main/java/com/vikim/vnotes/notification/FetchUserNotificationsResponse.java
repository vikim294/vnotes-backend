package com.vikim.vnotes.notification;

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
