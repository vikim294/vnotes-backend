package com.vikim.demo.admin.user;

import java.time.Instant;

public record GetUserListResponse(
        Long id,
        String email,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
