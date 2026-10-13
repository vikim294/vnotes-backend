package com.vikim.vnotes.admin.role;

import java.time.LocalDateTime;

public record GetRoleWithPermissionRow(
        Long id,
        String code,
        String name,
        String description,
        boolean builtIn,
        Long permissionId,
        String permissionCode,
        String permissionName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
