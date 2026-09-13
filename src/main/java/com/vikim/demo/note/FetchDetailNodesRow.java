package com.vikim.demo.note;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FetchDetailNodesRow(
        Long id,
        Long parentId,
        Long directoryNodeId,
        String label,
        String imageUrl,
        BigDecimal x,
        BigDecimal y,
        BigDecimal imageWidth,
        BigDecimal imageHeight,
        DetailNodeContentType contentType,
        boolean article,
        DetailNodeMarkType markType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
