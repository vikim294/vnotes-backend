package com.vikim.demo.outbox;


import com.vikim.demo.common.VnotesEvent;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OutboxEvent {
    private Long id;

    private String eventId;

    private VnotesEvent eventName;

    private String eventJson;

    private OutboxEventStatus status;

    private Instant createdAt;

    private Instant updatedAt;
}
