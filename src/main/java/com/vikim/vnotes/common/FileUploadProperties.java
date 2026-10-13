package com.vikim.vnotes.common;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "file")
public record FileUploadProperties(
        @NotBlank
        String uploadDir
) {
}
