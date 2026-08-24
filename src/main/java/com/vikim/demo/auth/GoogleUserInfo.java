package com.vikim.demo.auth;

public record GoogleUserInfo(
        String sub,
        String email,
        boolean emailVerified,
        String name
) {
}
