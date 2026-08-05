package com.vikim.demo.note;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateDirectoryNodeMapperParam {
    private Long id;

    private Long userId;

    private Long parentId;

    private String label;

    private BigDecimal x;

    private BigDecimal y;

    public UpdateDirectoryNodeMapperParam(Long userId, Long parentId, String label, BigDecimal x, BigDecimal y) {
        this.userId = userId;
        this.parentId = parentId;
        this.label = label;
        this.x = x;
        this.y = y;
    }
}
