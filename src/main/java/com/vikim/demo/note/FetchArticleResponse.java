package com.vikim.demo.note;

import java.time.Instant;
import java.time.ZoneOffset;

public record FetchArticleResponse(
        Long id,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
    public static FetchArticleResponse from(FetchArticleRow row) {
        return new FetchArticleResponse(
                row.id(),
                row.content(),
                row.createdAt().toInstant(ZoneOffset.UTC),
                row.updatedAt().toInstant(ZoneOffset.UTC)
        );
    }
}
