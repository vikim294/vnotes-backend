package com.vikim.demo.auth;

import com.vikim.demo.user.UserStatus;

public record UserAuthState(
        UserStatus status,
        Long tokenVersion
) {
}
