package com.vikim.vnotes.outbox;

public enum OutboxEventStatus {
    PENDING,
    SENDING,
    SENT,
    FAILED
}
