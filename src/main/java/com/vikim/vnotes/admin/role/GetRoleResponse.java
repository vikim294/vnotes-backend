package com.vikim.vnotes.admin.role;

import java.time.Instant;

public record GetRoleResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean builtIn,
        Instant createdAt,
        Instant updatedAt
) {
}
