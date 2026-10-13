package com.vikim.vnotes.admin.user;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "admin.bootstrap.super-admin")
public record InitSuperAdminProperties(
        String email,

        String password
) {
}
