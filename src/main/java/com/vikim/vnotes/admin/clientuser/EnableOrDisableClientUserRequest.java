package com.vikim.vnotes.admin.clientuser;

import com.vikim.vnotes.user.UserStatus;
import jakarta.validation.constraints.NotNull;

public record EnableOrDisableClientUserRequest(
        @NotNull(message = "userId 不能为空")
        Long userId,

        @NotNull(message = "status 不能为空")
        UserStatus status
) {
}
