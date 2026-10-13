package com.vikim.vnotes.notification;

import java.time.Instant;

public class Notification {
    private Long id;

    private NotificationType type;

    private Long userId;

    private String title;

    private String content;

    private Instant readAt;

    private boolean hasRead;

    private Instant createdAt;

    private Instant deletedAt;

}
