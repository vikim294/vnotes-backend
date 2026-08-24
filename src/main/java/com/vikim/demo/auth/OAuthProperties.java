package com.vikim.demo.auth;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "oauth")
public record OAuthProperties(
        @NotBlank
        String loginUrl,

        @NotBlank
        String googleClientId,

        @NotBlank
        String googleClientSecret,

        @NotBlank
        String googleRedirectUri
) {
}
