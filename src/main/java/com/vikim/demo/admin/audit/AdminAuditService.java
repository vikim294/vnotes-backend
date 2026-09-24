package com.vikim.demo.admin.audit;

import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuditService {
    private final AdminAuditMapper adminAuditMapper;

    public void createAuditLog(Long userId, AdminAuditAction action, String description) {
        if (adminAuditMapper.createAuditLog(new CreateAuditLogParam(
                userId,
                action,
                description
        )) != 1) {
            throw new BusinessException(ErrorCode.ADMIN_CREATE_AUDIT_LOG_FAILED);
        }
    }

    @PreAuthorize("hasAuthority('admin:audit:read')")
    public List<GetAuditLogResponse> getAuditLogs() {
        return adminAuditMapper.getAuditLogs().stream()
                .map(row -> new GetAuditLogResponse(
                        row.id(),
                        row.userId(),
                        row.action(),
                        row.description(),
                        row.createdAt().toInstant(ZoneOffset.UTC)
                ))
                .toList();
    }
}
