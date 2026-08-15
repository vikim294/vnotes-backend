package com.vikim.demo.note;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ImageDetailNodeResponse (
        Long id,
        Long parentId,
        Long directoryNodeId,
        String imageUrl,
        BigDecimal x,
        BigDecimal y,
        BigDecimal imageWidth,
        BigDecimal imageHeight,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
