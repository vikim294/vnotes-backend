package com.vikim.demo.mq.events;

public record UserRegisteredEvent(
        String eventId,
        Long userId
) {
}
