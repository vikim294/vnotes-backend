package com.vikim.demo.admin.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final AdminUserMapper adminUserMapper;

    @PreAuthorize("hasAuthority('admin:user:read')")
    public List<GetUserListResponse> getUserList() {
        return adminUserMapper.getUserList().stream()
                .map(row -> new GetUserListResponse(
                        row.id(),
                        row.email(),
                        row.status(),
                        row.createdAt().toInstant(ZoneOffset.UTC),
                        row.updatedAt().toInstant(ZoneOffset.UTC)
                ))
                .toList();
    }
}
