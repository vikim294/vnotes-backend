package com.vikim.demo.note;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DirectoryNode {
    private Long id;
    private String label;
    private Long userId;
    private Long parentId;
    private BigDecimal x;
    private BigDecimal y;
    private NodeType nodeType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
