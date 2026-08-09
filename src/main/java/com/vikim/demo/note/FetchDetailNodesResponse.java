package com.vikim.demo.note;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FetchDetailNodesResponse(
        Long id,
        Long parentId,
        Long directoryNodeId,
        String label,
        BigDecimal x,
        BigDecimal y,
        DetailNodeContentType contentType,
        DetailNodeMarkType markType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
