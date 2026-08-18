package com.vikim.demo.auth;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "resend")
@Profile("prod")
public record EmailSenderProperties(
        @NotBlank
        String apiKey
) {
}
