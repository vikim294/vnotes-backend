package com.vikim.demo.objectStorage;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.cos")
public record CosProperties(
        @NotBlank
        String secretId,

        @NotBlank
        String secretKey,

        @NotBlank
        String bucket,

        @NotBlank
        String region
) {
}
