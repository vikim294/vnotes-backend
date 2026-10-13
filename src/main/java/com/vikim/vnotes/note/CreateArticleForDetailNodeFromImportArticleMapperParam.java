package com.vikim.vnotes.note;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CreateArticleForDetailNodeFromImportArticleMapperParam {
    private Long detailNodeId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
