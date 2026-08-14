package com.vikim.demo.note;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExportDirectoryNodeVO(
        Long sourceId,
        Long sourceParentId,
        String label,
        BigDecimal x,
        BigDecimal y,
        NodeType nodeType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
