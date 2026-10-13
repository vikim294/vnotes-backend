package com.vikim.vnotes.admin.user;

import java.time.Instant;

public record GetUserListResponse(
        Long id,
        String email,
        AdminUserStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
