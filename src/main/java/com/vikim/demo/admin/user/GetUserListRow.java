package com.vikim.demo.admin.user;

import java.time.LocalDateTime;

public record GetUserListRow(
        Long id,
        String email,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
