package com.vikim.vnotes.note;

import java.time.Instant;
import java.time.ZoneOffset;

public record NoteShareResponse(
        Long id,
        Long directoryNodeId,
        String shareToken,
        NoteShareStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static NoteShareResponse from(NoteShareRow row){
        return new NoteShareResponse(
                row.id(),
                row.directoryNodeId(),
                row.shareToken(),
                row.status(),
                row.createdAt().toInstant(ZoneOffset.UTC),
                row.updatedAt().toInstant(ZoneOffset.UTC)
        );
    }
}
