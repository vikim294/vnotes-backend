package com.vikim.demo.note;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchDirectoryNodeMapperParam {
    private Long userId;

    private String keyword;

    private int limit;

    private int offset;

    private NodeType nodeType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public SearchDirectoryNodeMapperParam(Long userId, String keyword, int limit, int offset, NodeType nodeType, LocalDateTime startTime, LocalDateTime endTime) {
        this.userId = userId;
        this.keyword = keyword;
        this.limit = limit;
        this.offset = offset;
        this.nodeType = nodeType;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
