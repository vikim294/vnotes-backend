package com.vikim.demo.note;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ImportDetailNodeDTO(
        Long sourceId,
        Long sourceParentId,
        Long sourceDirectoryNodeId,
        String label,
        BigDecimal x,
        BigDecimal y,
        DetailNodeContentType contentType,
        DetailNodeMarkType markType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
