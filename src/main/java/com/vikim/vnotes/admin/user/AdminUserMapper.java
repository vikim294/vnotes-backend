package com.vikim.vnotes.admin.user;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface AdminUserMapper {

    @Select("""
            select exists(
                select 1
                from admin_user_roles ur
                join admin_roles r on r.id = ur.role_id
                where r.code = 'admin.super_admin'
            )
            """)
    boolean hasSuperAdmin();

    @Select("""
            select exists(
                select 1
                from admin_user_roles ur
                join admin_roles r on r.id = ur.role_id
                where ur.user_id = #{userId}
                    and r.built_in = true
            )
            """)
    boolean hasBuiltInRole(Long userId);

    boolean existsBuiltInRoleWithinRoles(Set<Long> roleIds);

    @Insert("""
            insert into admin_users(email, password_hash, status)
            values(#{email}, #{passwordHash}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int createUser(CreateAdminUserMapperParam request);

    @Insert("""
            insert into admin_user_roles(user_id, role_id, assigned_by)
            values(#{userId}, #{roleId}, #{assignedBy})
            """)
    int assignRole(@Param("userId") Long userId, @Param("roleId") Long roleId, @Param("assignedBy") Long assignedBy);

    int assignRolesToUser(@Param("userId") Long userId, @Param("roleIds") Set<Long> roleIds, @Param("assignedBy") Long assignedBy);

    @Delete("""
            delete from admin_user_roles
            where user_id = #{userId}
            """)
    int clearUserRoles(Long userId);

    @Select("""
            select id, email, password_hash, status
            from admin_users
            where email = #{email}
                and status = 'ACTIVE'
            """)
    Optional<GetUserResponse> getActiveUserByEmail(String email);

    @Select("""
            select exists(
                select 1
                from admin_users
                where id = #{userId}
            )
            """)
    boolean existsById(Long userId);

    // 暂不分页
    @Select("""
            select id, email, status, created_at, updated_at
            from admin_users
            """)
    List<GetUserListRow> getUserList();

    // 暂不分页
    @Select("""
            select
                u.id,
                u.email,
                u.status,
                r.id as role_id,
                r.name as role_name,
                r.built_in as role_built_in,
                u.created_at,
                u.updated_at
            from admin_users u
            left join admin_user_roles ur on ur.user_id = u.id
            left join admin_roles r on r.id = ur.role_id
            order by u.created_at desc, u.id desc
            """)
    List<GetUserWithRolesListRow> getUserWithRolesList();

    int enableOrDisableUser(@Param("userId") Long userId, @Param("status") AdminUserStatus status);
}
