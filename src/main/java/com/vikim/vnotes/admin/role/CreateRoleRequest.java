package com.vikim.vnotes.admin.role;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequest(
        @NotBlank(message = "角色 name 不能为空")
        String name,

        String description
) {
}
