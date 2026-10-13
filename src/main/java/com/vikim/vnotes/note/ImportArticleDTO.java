package com.vikim.vnotes.note;

import java.time.LocalDateTime;

public record ImportArticleDTO(
        Long sourceId,
        Long sourceDetailNodeId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
