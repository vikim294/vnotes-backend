package com.vikim.demo.admin.audit;

import jakarta.validation.constraints.NotNull;

public record CreateAuditLogParam(
        @NotNull(message = "userId 不能为空")
        Long userId,

        @NotNull(message = "action 不能为空")
        AdminAuditAction action,

        String description
) {
}
