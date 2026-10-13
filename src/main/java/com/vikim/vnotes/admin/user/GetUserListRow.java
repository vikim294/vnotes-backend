package com.vikim.vnotes.admin.user;

import java.time.LocalDateTime;

public record GetUserListRow(
        Long id,
        String email,
        AdminUserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
