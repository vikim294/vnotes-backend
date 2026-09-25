package com.vikim.demo.admin.user;

import java.time.Instant;
import java.util.List;

public record GetUserWithRolesListResponse(
        Long id,
        String email,
        UserStatus status,
        List<UserRole> roles,
        Instant createdAt,
        Instant updatedAt
) {
}
