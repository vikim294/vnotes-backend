package com.vikim.vnotes.note;

import java.time.LocalDateTime;

public record GetRecentAccessedNoteHistoryRow(
        Long id,
        Long directoryNodeId,
        String directoryNodeLabel,
        LocalDateTime accessedAt
) {
}
