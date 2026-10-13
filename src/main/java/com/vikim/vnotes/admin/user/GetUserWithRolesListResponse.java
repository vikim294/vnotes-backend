package com.vikim.vnotes.admin.user;

import java.time.Instant;
import java.util.List;

public record GetUserWithRolesListResponse(
        Long id,
        String email,
        AdminUserStatus status,
        List<UserRole> roles,
        Instant createdAt,
        Instant updatedAt
) {
}
