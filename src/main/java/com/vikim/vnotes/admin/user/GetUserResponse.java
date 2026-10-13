package com.vikim.vnotes.admin.user;

public record GetUserResponse(
        Long id,
        String email,
        String passwordHash,
        AdminUserStatus status
) {
}
