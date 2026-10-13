package com.vikim.vnotes.note;

import java.time.LocalDateTime;

public record NoteShareRow(
        Long id,
        Long directoryNodeId,
        String shareToken,
        NoteShareStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
