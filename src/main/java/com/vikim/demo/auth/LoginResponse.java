package com.vikim.demo.auth;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId
) {
}
