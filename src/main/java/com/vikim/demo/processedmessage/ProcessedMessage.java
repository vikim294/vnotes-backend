package com.vikim.demo.processedmessage;


import java.time.Instant;
import java.util.UUID;

public class ProcessedMessage {
    private Long id;

    private UUID eventId;

    private String consumer;

    private Instant createdAt;
}
