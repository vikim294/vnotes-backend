package com.vikim.demo.note;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DetailNode {
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
