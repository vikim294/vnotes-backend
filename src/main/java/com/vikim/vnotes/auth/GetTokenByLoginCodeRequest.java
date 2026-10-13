package com.vikim.vnotes.auth;

import jakarta.validation.constraints.NotBlank;

public record GetTokenByLoginCodeRequest(
        @NotBlank
        String code
) {
}
