package com.vikim.demo.note;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;

public record FetchDirectoryNodesResponse(
        Long id,
        Long parentId,
        String label,
        BigDecimal x,
        BigDecimal y,
        NodeType nodeType,
        Instant createdAt,
        Instant updatedAt
) {
    static FetchDirectoryNodesResponse from(DirectoryNodeRow row){
        return new FetchDirectoryNodesResponse(
                row.id(),
                row.parentId(),
                row.label(),
                row.x(),
                row.y(),
                row.nodeType(),
                row.createdAt().toInstant(ZoneOffset.UTC),
                row.updatedAt().toInstant(ZoneOffset.UTC)
        );
    }
}
