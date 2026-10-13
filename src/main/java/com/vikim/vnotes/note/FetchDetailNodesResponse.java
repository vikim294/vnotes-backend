package com.vikim.vnotes.note;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;

public record FetchDetailNodesResponse(
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
        Long refDirectoryNodeId,
        Long refDetailNodeId,
        Instant createdAt,
        Instant updatedAt
) {
    static FetchDetailNodesResponse from(FetchDetailNodesRow row){
        return new FetchDetailNodesResponse(
                row.id(),
                row.parentId(),
                row.directoryNodeId(),
                row.label(),
                row.imageUrl(),
                row.x(),
                row.y(),
                row.imageWidth(),
                row.imageHeight(),
                row.contentType(),
                row.article(),
                row.markType(),
                row.refDirectoryNodeId(),
                row.refDetailNodeId(),
                row.createdAt().toInstant(ZoneOffset.UTC),
                row.updatedAt().toInstant(ZoneOffset.UTC)
        );
    }
}
