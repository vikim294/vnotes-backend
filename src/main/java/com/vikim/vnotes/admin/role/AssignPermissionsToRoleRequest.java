package com.vikim.vnotes.admin.role;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record AssignPermissionsToRoleRequest(
        @NotNull(message = "roleId 不能为空")
        Long roleId,

        @NotNull(message = "permissionIds 必传")
        Set<@NotNull Long> permissionIds
) {
}
