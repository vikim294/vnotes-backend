package com.vikim.vnotes.note;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ImportDetailNodeDTO(
        Long sourceId,
        Long sourceParentId,
        Long sourceDirectoryNodeId,
        String label,
        BigDecimal x,
        BigDecimal y,
        BigDecimal imageWidth,
        BigDecimal imageHeight,
        DetailNodeContentType contentType,
        DetailNodeMarkType markType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
