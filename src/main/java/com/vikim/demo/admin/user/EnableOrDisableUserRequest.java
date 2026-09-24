package com.vikim.demo.admin.user;

import jakarta.validation.constraints.NotNull;

public record EnableOrDisableUserRequest(
        @NotNull(message = "userId 不能为空")
        Long userId,

        @NotNull(message = "status 不能为空")
        UserStatus status
) {
}
