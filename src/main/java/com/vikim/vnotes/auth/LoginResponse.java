package com.vikim.vnotes.auth;

public record LoginResponse(
        String accessToken,
        long expiresIn,
        Long userId
) {
}
