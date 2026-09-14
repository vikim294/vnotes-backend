package com.vikim.demo.note;

import java.time.LocalDateTime;

public record ExportArticleVO(
        Long sourceId,
        Long sourceDetailNodeId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
