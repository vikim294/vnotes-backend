package com.vikim.demo.admin.user;

import java.time.LocalDateTime;

public record GetUserWithRolesListRow(
        Long id,
        String email,
        UserStatus status,
        Long roleId,
        String roleName,
        Boolean roleBuiltIn,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
