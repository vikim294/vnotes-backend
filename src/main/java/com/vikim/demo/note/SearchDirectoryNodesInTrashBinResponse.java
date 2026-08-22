package com.vikim.demo.note;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;

public record SearchDirectoryNodesInTrashBinResponse(
        Long id,
        String label,
        BigDecimal x,
        BigDecimal y,
        NodeType nodeType,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
    static SearchDirectoryNodesInTrashBinResponse from(SearchDirectoryNodesInTrashBinRow row){
        return new SearchDirectoryNodesInTrashBinResponse(
                row.id(),
                row.label(),
                row.x(),
                row.y(),
                row.nodeType(),
                row.createdAt().toInstant(ZoneOffset.UTC),
                row.updatedAt().toInstant(ZoneOffset.UTC),
                row.deletedAt().toInstant(ZoneOffset.UTC)
        );
    }
}
