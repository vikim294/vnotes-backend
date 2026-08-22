package com.vikim.demo.note;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DirectoryNodeRow(
        Long id,
        Long parentId,
        String label,
        BigDecimal x,
        BigDecimal y,
        NodeType nodeType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
