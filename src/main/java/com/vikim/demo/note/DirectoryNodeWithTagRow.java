package com.vikim.demo.note;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DirectoryNodeWithTagRow(
        Long id,
        Long parentId,
        String label,
        BigDecimal x,
        BigDecimal y,
        NodeType nodeType,
        boolean shared,
        Long tagId,
        String tagName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
