package com.vikim.vnotes.admin.role;

import java.time.Instant;
import java.util.List;

public record GetRoleWithPermissionResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean builtIn,
        List<RolePermission> permissions,
        Instant createdAt,
        Instant updatedAt
) {
}
