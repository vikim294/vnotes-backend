package com.vikim.demo.note;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateDetailNodeMapperParam {
    private Long id;

    private Long parentId;

    private Long directoryNodeId;

    private String label;

    private BigDecimal x;

    private BigDecimal y;

    private boolean parentIdPresent;

    private DetailNodeMarkType markType;

    private boolean markTypePresent;

    public UpdateDetailNodeMapperParam(Long directoryNodeId, Long parentId, String label, BigDecimal x, BigDecimal y, DetailNodeMarkType markType, boolean markTypePresent) {
        this.directoryNodeId = directoryNodeId;
        this.parentId = parentId;
        this.label = label;
        this.x = x;
        this.y = y;
        this.markType = markType;
        this.markTypePresent = markTypePresent;
    }
}
