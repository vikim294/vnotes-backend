package com.vikim.vnotes.admin.permission;

import java.time.LocalDateTime;

public record GetPermissionRow(
        Long id,
        String code,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
