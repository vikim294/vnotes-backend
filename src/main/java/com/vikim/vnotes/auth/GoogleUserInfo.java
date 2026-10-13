package com.vikim.vnotes.auth;

public record GoogleUserInfo(
        String sub,
        String email,
        boolean emailVerified,
        String name
) {
}
