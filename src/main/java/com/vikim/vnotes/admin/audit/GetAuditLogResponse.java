package com.vikim.vnotes.admin.audit;

import java.time.Instant;

public record GetAuditLogResponse(
        Long id,
        Long userId,
        AdminAuditAction action,
        String description,
        Instant createdAt
) {
}
