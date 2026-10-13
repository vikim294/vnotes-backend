package com.vikim.vnotes.note;

import java.time.Instant;

public record GetRecentAccessedNoteHistoryResponse(
        Long id,
        Long directoryNodeId,
        String directoryNodeLabel,
        Instant accessedAt
) {
}
