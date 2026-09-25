package com.vikim.demo.admin.user;

import com.vikim.demo.admin.audit.AdminAuditAction;
import com.vikim.demo.admin.audit.AdminAuditService;
import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final AdminAuditService adminAuditService;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "12345678";

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

    @PreAuthorize("hasAuthority('admin:user:read')")
    public List<GetUserWithRolesListResponse> getUserWithRolesList() {
        List<GetUserWithRolesListRow> rows = adminUserMapper.getUserWithRolesList();

        Map<Long, GetUserWithRolesListResponse> listResponseMap = new LinkedHashMap<>();

        for (GetUserWithRolesListRow row : rows) {
            GetUserWithRolesListResponse item = listResponseMap.get(row.id());

            UserRole role = new UserRole(
                    row.roleId(),
                    row.roleName(),
                    row.roleBuiltIn()
            );

            if (item == null) {
                List<UserRole> roles = new ArrayList<>();

                if (row.roleId() != null) {
                    roles.add(role);
                }

                listResponseMap.put(row.id(), new GetUserWithRolesListResponse(
                        row.id(),
                        row.email(),
                        row.status(),
                        roles,
                        row.createdAt().toInstant(ZoneOffset.UTC),
                        row.updatedAt().toInstant(ZoneOffset.UTC)
                ));
            } else {
                if (row.roleId() != null) {
                    item.roles().add(role);
                }
            }
        }

        return listResponseMap.values().stream().toList();
    }

    @PreAuthorize("hasAuthority('admin:user:add')")
    @Transactional
    public void addUser(Long userId, CreateAdminUserRequest request) {
        CreateAdminUserMapperParam user = new CreateAdminUserMapperParam();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setStatus(AdminUserStatus.ACTIVE);

        try {
            if (adminUserMapper.createUser(user) != 1) {
                throw new BusinessException(ErrorCode.ADMIN_USER_CREATED_FAILED);
            }
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.ADMIN_USER_ALREADY_EXISTS);
        }

        adminAuditService.createAuditLog(
                userId,
                AdminAuditAction.ADD_USER,
                "新增用户：" + request.getEmail()
        );
    }

    @PreAuthorize("hasAuthority('admin:user:enable_and_disable')")
    @Transactional
    public void enableOrDisableUser(
            Long operatorUserId,
            EnableOrDisableUserRequest request
    ) {
        // 判断目标用户是否存在
        if (!adminUserMapper.existsById(request.userId())) {
            throw new BusinessException(ErrorCode.ADMIN_USER_NOT_FOUND);
        }

        // 禁止操作拥有系统内置角色的用户
        if (adminUserMapper.hasBuiltInRole(request.userId())) {
            throw new BusinessException(ErrorCode.ADMIN_OPERATION_DENIED);
        }

        // 不能启用和禁用自己
        if (operatorUserId.equals(request.userId())) {
            throw new BusinessException(
                    ErrorCode.ADMIN_OPERATION_DENIED
            );
        }

        if (adminUserMapper.enableOrDisableUser(request.userId(), request.status()) == 1) {
            adminAuditService.createAuditLog(
                    operatorUserId,
                    request.status() == AdminUserStatus.ACTIVE ? AdminAuditAction.ENABLE_USER : AdminAuditAction.DISABLE_USER,
                    (request.status() == AdminUserStatus.ACTIVE ? "启用" : "禁用") + "用户：" + request.userId()
            );
        } else {
            // 第一次查询之后，目标用户可能被其他并发请求删除
            if (!adminUserMapper.existsById(request.userId())) {
                throw new BusinessException(ErrorCode.ADMIN_USER_NOT_FOUND);
            }

            // 用户仍然存在，说明状态原本就是目标状态，按幂等成功处理，什么也不做，响应成功
        }
    }

    @PreAuthorize("hasAuthority('admin:user:assign_role')")
    @Transactional
    public void assignRolesToUser(
            Long userId,
            AssignRolesToUserRequest request
    ) {
        // 判断目标用户是否存在
        if (!adminUserMapper.existsById(request.userId())) {
            throw new BusinessException(ErrorCode.ADMIN_USER_NOT_FOUND);
        }

        // 禁止操作拥有系统内置角色的用户
        if (adminUserMapper.hasBuiltInRole(request.userId())) {
            throw new BusinessException(ErrorCode.ADMIN_OPERATION_DENIED);
        }

        // 禁止分配系统角色给用户
        if (!request.roleIds().isEmpty() && adminUserMapper.existsBuiltInRoleWithinRoles(request.roleIds())) {
            throw new BusinessException(ErrorCode.ADMIN_OPERATION_DENIED);
        }

        // 清空用户角色
        adminUserMapper.clearUserRoles(request.userId());

        if (!request.roleIds().isEmpty()) {
            // 清空用户的角色后，重新指定角色给用户
            if (adminUserMapper.assignRolesToUser(request.userId(), request.roleIds(), userId) != request.roleIds().size()) {
                throw new BusinessException(ErrorCode.ADMIN_ASSIGN_ROLE_FAILED);
            }
        }

        adminAuditService.createAuditLog(
                userId,
                AdminAuditAction.ASSIGN_ROLE,
                "为用户：" + request.userId() + "指定角色：" + request.roleIds()
        );
    }
}
