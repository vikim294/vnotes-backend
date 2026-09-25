package com.vikim.demo.admin.user;

public record GetUserResponse(
        Long id,
        String email,
        String passwordHash,
        AdminUserStatus status
) {
}
