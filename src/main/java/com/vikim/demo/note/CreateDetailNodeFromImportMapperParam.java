package com.vikim.demo.note;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateDetailNodeFromImportMapperParam {
    private Long id;

    private Long parentId;

    private Long directoryNodeId;

    private String label;

    private BigDecimal x;

    private BigDecimal y;

    private DetailNodeContentType contentType;

    private DetailNodeMarkType markType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public CreateDetailNodeFromImportMapperParam(Long directoryNodeId, Long parentId, String label , BigDecimal x, BigDecimal y, DetailNodeContentType contentType, DetailNodeMarkType markType, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.directoryNodeId = directoryNodeId;
        this.parentId = parentId;
        this.label = label;
        this.x = x;
        this.y = y;
        this.contentType = contentType;
        this.markType = markType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
