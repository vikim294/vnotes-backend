package com.vikim.demo.note;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DetailNode {
    private Long id;
    private Long parentId;
    private Long directoryNodeId;
    private String label;
    private BigDecimal x;
    private BigDecimal y;
    private DetailNodeContentType contentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
