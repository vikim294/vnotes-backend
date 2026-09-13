package com.vikim.demo.note;

import java.time.LocalDateTime;

public record GetNoteShareArticleRow(
        Long id,
        String content,
        Long noteShareId,
        Long directoryNodeId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
