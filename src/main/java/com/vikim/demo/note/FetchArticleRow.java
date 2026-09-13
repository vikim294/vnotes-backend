package com.vikim.demo.note;

import java.time.LocalDateTime;

public record FetchArticleRow(
    Long id,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
