package com.vikim.vnotes.admin.user;

import com.vikim.vnotes.admin.role.AdminRoleMapper;
import com.vikim.vnotes.common.BusinessException;
import com.vikim.vnotes.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class InitSuperAdminService {

    private final AdminUserMapper adminUserMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final InitSuperAdminProperties properties;
    private final PasswordEncoder passwordEncoder;

    public boolean hasSuperAdmin() {
        return adminUserMapper.hasSuperAdmin();
    }

    @Transactional
    public void initSuperAdmin() {
        if (!StringUtils.hasText(properties.email())
                || !StringUtils.hasText(properties.password())) {
            throw new IllegalStateException(
                    "首次启动必须配置超级管理员邮箱和密码"
            );
        }

        CreateAdminUserMapperParam user = new CreateAdminUserMapperParam();
        user.setEmail(properties.email());
        user.setPasswordHash(passwordEncoder.encode(properties.password()));
        user.setStatus(AdminUserStatus.ACTIVE);

        // 创建用户
        if (adminUserMapper.createUser(user) != 1 || user.getId() == null) {
            throw new BusinessException(ErrorCode.ADMIN_USER_CREATED_FAILED);
        }

        // 指定 super admin 角色
        Long superAdminRoleId = adminRoleMapper.getIdByCode("admin.super_admin").orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ROLE_NOT_FOUND));
        if(adminUserMapper.assignRole(user.getId(), superAdminRoleId, null) != 1) {
            throw new BusinessException(ErrorCode.ADMIN_ASSIGN_ROLE_FAILED);
        }
    }
}
