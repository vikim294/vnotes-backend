package com.vikim.vnotes.admin.role;

import com.vikim.vnotes.admin.audit.AdminAuditAction;
import com.vikim.vnotes.admin.audit.AdminAuditService;
import com.vikim.vnotes.common.BusinessException;
import com.vikim.vnotes.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminRoleService {
    private final AdminRoleMapper adminRoleMapper;
    private final AdminAuditService adminAuditService;

    @PreAuthorize("hasAuthority('admin:role:read')")
    public List<GetRoleResponse> getAllRoles() {
        return adminRoleMapper.getAllRoles().stream()
                .map(row -> new GetRoleResponse(
                        row.id(),
                        row.code(),
                        row.name(),
                        row.description(),
                        row.builtIn(),
                        row.createdAt().toInstant(ZoneOffset.UTC),
                        row.updatedAt().toInstant(ZoneOffset.UTC)
                ))
                .toList();
    }

    @PreAuthorize("hasAuthority('admin:role:read')")
    public List<GetRoleWithPermissionResponse> getAllRolesWithPermissions() {
        List<GetRoleWithPermissionRow> rows = adminRoleMapper.getAllRolesWithPermissions();

        Map<Long, GetRoleWithPermissionResponse> listResponseMap = new LinkedHashMap<>();

        for (GetRoleWithPermissionRow row : rows) {
            GetRoleWithPermissionResponse item = listResponseMap.get(row.id());

            RolePermission permission = new RolePermission(
                    row.permissionId(),
                    row.permissionCode(),
                    row.permissionName()
            );

            if (item == null) {
                List<RolePermission> permissions = new ArrayList<>();

                if (row.permissionId() != null) {
                    permissions.add(permission);
                }

                listResponseMap.put(row.id(), new GetRoleWithPermissionResponse(
                        row.id(),
                        row.code(),
                        row.name(),
                        row.description(),
                        row.builtIn(),
                        permissions,
                        row.createdAt().toInstant(ZoneOffset.UTC),
                        row.updatedAt().toInstant(ZoneOffset.UTC)
                ));
            } else {
                if (row.permissionId() != null) {
                    item.permissions().add(permission);
                }
            }
        }

        return listResponseMap.values().stream().toList();
    }


    @PreAuthorize("hasAuthority('admin:role:assign_permission')")
    @Transactional
    public void assignPermissionsToRole(Long userId, AssignPermissionsToRoleRequest request) {
        // 先判断角色存在性
        GetRoleRow role = adminRoleMapper.getRoleById(request.roleId()).orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ROLE_NOT_FOUND));

        // 禁止操作系统内置角色
        if (role.builtIn()) {
            throw new BusinessException(ErrorCode.ADMIN_OPERATION_DENIED);
        }

        // 清空角色权限
        adminRoleMapper.clearRolePermissions(request.roleId());

        if (!request.permissionIds().isEmpty()) {
            // 清空角色权限后，重新指定权限给角色
            if (adminRoleMapper.assignPermissionsToRole(request.roleId(), request.permissionIds()) != request.permissionIds().size()) {
                throw new BusinessException(ErrorCode.ADMIN_ASSIGN_PERMISSION_FAILED);
            }
        }

        adminAuditService.createAuditLog(
                userId,
                AdminAuditAction.ASSIGN_PERMISSION,
                "为角色：" + request.roleId() + "指定权限：" + request.permissionIds()
        );
    }

    @PreAuthorize("hasAuthority('admin:role:delete')")
    @Transactional
    public void deleteRoleById(Long userId, Long id) {
        // 先判断角色存在性
        GetRoleRow role = adminRoleMapper.getRoleById(id).orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ROLE_NOT_FOUND));

        // 禁止操作系统内置角色
        if (role.builtIn()) {
            throw new BusinessException(ErrorCode.ADMIN_OPERATION_DENIED);
        }

        // 删除角色
        if (adminRoleMapper.deleteRoleById(id) != 1) {
            // delete sql 影响行为 0
            // 可以抛角色没找到异常
            // 也可以直接返回，告知删除成功
            // 但当前不是在“批量删除”语境下，所以还是明确告知”角色没找到“更好
            throw new BusinessException(ErrorCode.ADMIN_ROLE_NOT_FOUND);
        }

        adminAuditService.createAuditLog(
                userId,
                AdminAuditAction.DELETE_ROLE,
                "删除角色：" + id
        );
    }

    @PreAuthorize("hasAuthority('admin:role:add')")
    @Transactional
    public void createRole(Long userId, CreateRoleRequest request) {
        String description = request.description();
        if(description != null) {
            description = description.strip();

            if(description.isBlank()) {
                description = null;
            }
        }

        CreateRoleMapperParam param = new CreateRoleMapperParam(
                UUID.randomUUID().toString(),
                request.name().strip(),
                description
        );

        // 插入成功 -> 1
        // 唯一约束冲突 -> 抛 DuplicateKeyException
        // 其他数据库异常 -> 全局异常处理
        // 非预期影响行数 -> ADMIN_CREATE_ROLE_FAILED
        try {
            if (adminRoleMapper.createRole(param) != 1) {
                throw new BusinessException(ErrorCode.ADMIN_CREATE_ROLE_FAILED);
            }
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.ADMIN_ROLE_NAME_ALREADY_EXISTS);
        }

        adminAuditService.createAuditLog(
                userId,
                AdminAuditAction.CREATE_ROLE,
                "创建角色：" + param.name()
        );
    }

    @PreAuthorize("hasAuthority('admin:role:edit')")
    @Transactional
    public void updateRole(Long userId, UpdateRoleRequest request) {
        // 先判断角色存在性
        GetRoleRow role = adminRoleMapper.getRoleById(request.id()).orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ROLE_NOT_FOUND));

        // 禁止操作系统内置角色
        if (role.builtIn()) {
            throw new BusinessException(ErrorCode.ADMIN_OPERATION_DENIED);
        }

        String description = request.description();
        if(description != null) {
            description = description.strip();

            if(description.isBlank()) {
                description = null;
            }
        }

        UpdateRoleMapperParam param = new UpdateRoleMapperParam(
                request.id(),
                request.name().strip(),
                description
        );

        // name 和 描述没变时，直接返回
        if (param.getName().equals(role.name()) && Objects.equals(param.getDescription(), role.description())) {
            return;
        }

        // 匹配到记录了 -> 1（可能实际更新了，也可能没实际更新，总之更新成功了）
        // 影响 0 行 -> SQL 执行成功，但没有记录被匹配或修改（查询完成后、执行更新前，角色被另一个请求删除了）
        // 出现数据库重复键异常 -> 角色名已存在
        // 出现其他数据库异常 -> 全局异常处理
        int affectedRows;

        try {
            affectedRows = adminRoleMapper.updateRole(param);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.ADMIN_ROLE_NAME_ALREADY_EXISTS);
        }

        if(affectedRows != 1) {
            // 查询后可能被其他请求删除
            throw new BusinessException(ErrorCode.ADMIN_ROLE_NOT_FOUND);
        }

        adminAuditService.createAuditLog(
                userId,
                AdminAuditAction.UPDATE_ROLE,
                "更新角色名称：" + param.getName() + "，描述：" + param.getDescription()
        );
    }
}
