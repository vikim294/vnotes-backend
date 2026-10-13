package com.vikim.vnotes.mq.events;

public record UserRegisteredEvent(
        String eventId,
        Long userId
) {
}
