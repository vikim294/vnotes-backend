package com.vikim.vnotes.admin.audit;

import java.time.LocalDateTime;

public record GetAuditLogForExportRow(
        Long id,
        Long userId,
        String email,
        AdminAuditAction action,
        String description,
        LocalDateTime createdAt
) {
}
