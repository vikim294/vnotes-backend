package com.vikim.demo.admin.user;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

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

    @Select("""
            select id, email, password_hash, status
            from admin_users
            where email = #{email}
                and status = 'ACTIVE'
            """)
    Optional<GetUserResponse> getUserByEmail(String email);

    // 暂不分页
    @Select("""
            select id, email, status, created_at, updated_at
            from admin_users
            """)
    List<GetUserListRow> getUserList();
}
