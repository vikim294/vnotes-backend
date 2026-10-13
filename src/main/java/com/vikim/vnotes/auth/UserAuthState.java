package com.vikim.vnotes.auth;

import com.vikim.vnotes.user.UserStatus;

public record UserAuthState(
        UserStatus status,
        Long tokenVersion
) {
}
