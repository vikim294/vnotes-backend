package com.vikim.vnotes.outbox;


import com.vikim.vnotes.common.VnotesEvent;
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

    private Instant claimedAt;

    private Long claimVersion;

    private Instant updatedAt;
}
