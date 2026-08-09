package com.vikim.demo.note;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDetailNodeMapperParam {
    private Long id;

    private Long parentId;

    private Long directoryNodeId;

    private String label;

    private BigDecimal x;

    private BigDecimal y;

    private DetailNodeContentType contentType;

    public CreateDetailNodeMapperParam(Long directoryNodeId, String label, BigDecimal x, BigDecimal y, DetailNodeContentType contentType) {
        this.directoryNodeId = directoryNodeId;
        this.label = label;
        this.x = x;
        this.y = y;
        this.contentType = contentType;
    }
}
