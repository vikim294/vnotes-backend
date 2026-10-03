package com.vikim.demo.note;

import com.vikim.demo.tag.SimpleTagResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class FetchDirectoryNodesWithTagsResponse {
    private Long id;
    private Long parentId;
    private List<String> path;
    private String label;
    private BigDecimal x;
    private BigDecimal y;
    private NodeType nodeType;
    private boolean shared;
    private Set<SimpleTagResponse> tags;
    private Instant createdAt;
    private Instant updatedAt;
}
