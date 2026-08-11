package com.vikim.demo.user;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String avatarUrl,
        LocalDateTime createdAt
) {
}
