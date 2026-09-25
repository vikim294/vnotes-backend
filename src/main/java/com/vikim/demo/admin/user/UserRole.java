package com.vikim.demo.admin.user;

public record UserRole(
        Long id,
        String name,
        Boolean builtIn
) {
}
