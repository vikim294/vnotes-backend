package com.vikim.demo.note;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDetailNodeMapperParam {
    private Long id;

    private Long parentId;

    private Long directoryNodeId;

    private String label;

    private String imageUrl;

    private BigDecimal x;

    private BigDecimal y;

    private BigDecimal imageWidth;

    private BigDecimal imageHeight;

    private DetailNodeContentType contentType;

    private DetailNodeMarkType markType;

    public CreateDetailNodeMapperParam(Long directoryNodeId, String label, String imageUrl, BigDecimal x, BigDecimal y, BigDecimal imageWidth, BigDecimal imageHeight, DetailNodeContentType contentType, DetailNodeMarkType markType) {
        this.directoryNodeId = directoryNodeId;
        this.label = label;
        this.imageUrl = imageUrl;
        this.x = x;
        this.y = y;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.contentType = contentType;
        this.markType = markType;
    }
}
