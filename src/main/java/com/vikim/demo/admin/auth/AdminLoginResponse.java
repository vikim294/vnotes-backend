package com.vikim.demo.admin.auth;

import java.util.List;

public record AdminLoginResponse(
        String accessToken,
        long expiresIn,
        List<String> roles,
        List<String> permissions
) {
}
