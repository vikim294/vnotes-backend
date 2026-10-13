package com.vikim.vnotes.admin.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPermissionService {
    private final AdminPermissionMapper adminPermissionMapper;

    @PreAuthorize("hasAuthority('admin:permission:read')")
    public List<GetPermissionResponse> getAllPermissions() {
        return adminPermissionMapper.getAllPermissions().stream()
                .map(row -> new GetPermissionResponse(
                        row.id(),
                        row.code(),
                        row.name(),
                        row.description(),
                        row.createdAt().toInstant(ZoneOffset.UTC),
                        row.updatedAt().toInstant(ZoneOffset.UTC)
                ))
                .toList();
    }
}
