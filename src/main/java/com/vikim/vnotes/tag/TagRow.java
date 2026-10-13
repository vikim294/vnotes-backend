package com.vikim.vnotes.tag;

import java.time.LocalDateTime;

public record TagRow(
        Long id,
        String name,
        LocalDateTime createdAt
) {
}
