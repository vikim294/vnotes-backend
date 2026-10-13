package com.vikim.vnotes.note;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDirectoryNodeMapperParam {
    private Long id;

    private Long userId;

    private Long parentId;

    private String label;

    private BigDecimal x;

    private BigDecimal y;

    private NodeType nodeType;

    public CreateDirectoryNodeMapperParam(Long userId, String label, BigDecimal x, BigDecimal y, NodeType nodeType) {
        this.userId = userId;
        this.label = label;
        this.x = x;
        this.y = y;
        this.nodeType = nodeType;
    }
}
