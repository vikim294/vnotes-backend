package com.vikim.demo.mq.events;

import java.util.UUID;

public record UserRegisteredEvent(
        UUID eventId,
        Long userId
) {
}
