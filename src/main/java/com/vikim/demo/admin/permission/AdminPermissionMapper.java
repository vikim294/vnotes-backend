package com.vikim.demo.admin.permission;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminPermissionMapper {

    // 找出用户的所有权限 code
    // 不同角色 可能有重复的权限
    @Select("""
            select distinct p.code
            from admin_user_roles ur
            join admin_role_permissions rp on rp.role_id = ur.role_id
            join admin_permissions p on p.id = rp.permission_id
            join admin_users u on u.id = ur.user_id
            where u.id = #{userId}
                and u.status = 'ACTIVE'
            """)
    List<String> getUserAllPermissionCodes(Long userId);

    @Select("""
            select *
            from admin_permissions
            """)
    List<GetPermissionRow> getAllPermissions();
}
