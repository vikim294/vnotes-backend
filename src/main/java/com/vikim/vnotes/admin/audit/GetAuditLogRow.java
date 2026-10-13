package com.vikim.vnotes.admin.audit;

import java.time.LocalDateTime;

public record GetAuditLogRow(
        Long id,
        Long userId,
        AdminAuditAction action,
        String description,
        LocalDateTime createdAt
) {
}
