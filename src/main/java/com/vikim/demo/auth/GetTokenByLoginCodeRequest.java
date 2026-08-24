package com.vikim.demo.auth;

import jakarta.validation.constraints.NotBlank;

public record GetTokenByLoginCodeRequest(
        @NotBlank
        String code
) {
}
