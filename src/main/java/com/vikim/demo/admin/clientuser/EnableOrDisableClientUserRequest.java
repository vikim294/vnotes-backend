package com.vikim.demo.admin.clientuser;

import com.vikim.demo.user.UserStatus;
import jakarta.validation.constraints.NotNull;

public record EnableOrDisableClientUserRequest(
        @NotNull(message = "userId 不能为空")
        Long userId,

        @NotNull(message = "status 不能为空")
        UserStatus status
) {
}
