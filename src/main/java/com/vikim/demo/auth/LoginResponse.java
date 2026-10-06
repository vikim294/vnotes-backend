package com.vikim.demo.auth;

public record LoginResponse(
        String accessToken,
        long expiresIn,
        Long userId
) {
}
