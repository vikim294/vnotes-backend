package com.vikim.demo.admin.user;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record AssignRolesToUserRequest(
        @NotNull(message = "用户不能为空")
        Long userId,

        @NotNull(message = "roleIds 必传")
        Set<@NotNull Long> roleIds
) {
}
