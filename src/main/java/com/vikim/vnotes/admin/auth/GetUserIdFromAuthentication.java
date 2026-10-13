package com.vikim.vnotes.admin.auth;

import org.springframework.security.core.Authentication;

public class GetUserIdFromAuthentication {

    public static Long getIdFromAuthentication(
            Authentication authentication
    ) {
        return Long.valueOf(authentication.getName());
    }
}
