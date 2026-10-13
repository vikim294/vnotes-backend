package com.vikim.vnotes.admin.clientuser;

import com.vikim.vnotes.user.UserStatus;
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
