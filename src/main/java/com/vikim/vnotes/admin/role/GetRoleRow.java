package com.vikim.vnotes.admin.role;

import java.time.LocalDateTime;

public record GetRoleRow(
        Long id,
        String code,
        String name,
        String description,
        boolean builtIn,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
