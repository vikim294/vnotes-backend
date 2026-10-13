package com.vikim.vnotes.note;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateDirectoryNodeFromImportMapperParam {
    private Long id;

    private Long userId;

    private Long parentId;

    private String label;

    private BigDecimal x;

    private BigDecimal y;

    private NodeType nodeType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public CreateDirectoryNodeFromImportMapperParam(Long userId, Long parentId, String label, BigDecimal x, BigDecimal y, NodeType nodeType, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.parentId = parentId;
        this.label = label;
        this.x = x;
        this.y = y;
        this.nodeType = nodeType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
