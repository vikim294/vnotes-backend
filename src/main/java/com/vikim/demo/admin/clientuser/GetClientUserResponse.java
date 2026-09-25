package com.vikim.demo.admin.clientuser;

import com.vikim.demo.user.UserStatus;
import java.time.Instant;

public record GetClientUserResponse(
        Long id,
        String username,
        String email,
        String avatarUrl,
        UserStatus status,
        Long noteCount,
        Long sharedNoteCount,
        Long articleCount,
        Instant createdAt
) {
}
