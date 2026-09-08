package com.vikim.demo.note;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class SearchDirectoryNodeMapperParam {
    private Long userId;

    private String keyword;

    private int limit;

    private int offset;

    private NodeType nodeType;

    private Set<Long> tagIds;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public SearchDirectoryNodeMapperParam(Long userId, String keyword, int limit, int offset, NodeType nodeType, Set<Long> tagIds, LocalDateTime startTime, LocalDateTime endTime) {
        this.userId = userId;
        this.keyword = keyword;
        this.limit = limit;
        this.offset = offset;
        this.nodeType = nodeType;
        this.tagIds = tagIds;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
