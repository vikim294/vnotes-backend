package com.vikim.vnotes.admin.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull(message = "角色 id 不能为空")
        Long id,

        @NotBlank(message = "角色 name 不能为空")
        String name,

        String description
) {
}
