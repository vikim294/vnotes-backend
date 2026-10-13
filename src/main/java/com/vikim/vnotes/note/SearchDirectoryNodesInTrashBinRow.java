package com.vikim.vnotes.note;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SearchDirectoryNodesInTrashBinRow(
        Long id,
        String label,
        BigDecimal x,
        BigDecimal y,
        NodeType nodeType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
}
