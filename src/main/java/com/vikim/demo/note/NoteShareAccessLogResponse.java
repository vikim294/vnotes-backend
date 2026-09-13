package com.vikim.demo.note;

import java.time.Instant;
import java.time.ZoneOffset;

public record NoteShareAccessLogResponse(
        Long id,
        NoteShareTarget target,
        String targetLabel,
        Instant createdAt
) {
    public static NoteShareAccessLogResponse from(NoteShareAccessLogRow row){
        return new NoteShareAccessLogResponse(
                row.id(),
                row.target(),
                row.targetLabel(),
                row.createdAt().toInstant(ZoneOffset.UTC)
        );
    }
}
