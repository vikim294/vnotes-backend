package com.vikim.vnotes.tag;

import java.time.Instant;

public record TagResponse(
        Long id,
        String name,
        Instant createdAt
) {
}
