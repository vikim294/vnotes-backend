package com.vikim.vnotes.admin.clientuser;

import com.vikim.vnotes.user.UserStatus;
import java.time.LocalDateTime;

public record GetClientUserRow(
        Long id,
        String username,
        String email,
        String avatarUrl,
        UserStatus status,
        Long noteCount,
        Long sharedNoteCount,
        Long articleCount,
        LocalDateTime createdAt
) {
}
