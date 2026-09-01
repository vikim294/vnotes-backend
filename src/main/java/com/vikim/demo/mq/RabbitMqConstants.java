package com.vikim.demo.mq;

public final class RabbitMqConstants {
    public static final String USER_EXCHANGE = "user";
    public static final String DEAD_LETTER_EXCHANGE = "dead-letter";
    public static final String USER_REGISTERED_ROUTING_KEY = "user.registered";
    public static final String EMAIL_USER_REGISTERED_FAILED_ROUTING_KEY = "email.user.registered.failed";
    public static final String NOTIFICATION_USER_REGISTERED_FAILED_ROUTING_KEY = "notification.user.registered.failed";
    public static final String NOTIFICATION_USER_REGISTERED_QUEUE = "notification.user.registered";
    public static final String EMAIL_USER_REGISTERED_QUEUE = "email.user.registered";
    public static final String EMAIL_USER_DLQ = "email.user.dlq";
    public static final String NOTIFICATION_USER_DLQ = "notification.user.dlq";
    public static final String NOTIFICATION_USER_REGISTERED_CONSUMER = "notification.user.registered";
}
