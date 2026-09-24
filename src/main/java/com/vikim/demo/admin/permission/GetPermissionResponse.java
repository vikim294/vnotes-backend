package com.vikim.demo.admin.permission;

import java.time.Instant;

public record GetPermissionResponse(
        Long id,
        String code,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
