package com.vikim.vnotes.admin.user;

public record UserRole(
        Long id,
        String name,
        Boolean builtIn
) {
}
