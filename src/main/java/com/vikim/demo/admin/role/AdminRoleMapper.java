package com.vikim.demo.admin.role;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface AdminRoleMapper {

    @Select("""
            select id
            from admin_roles
            where code = #{code}
            """)
    Optional<Long> getIdByCode(String code);

    @Select("""
            select *
            from admin_roles
            where id = #{id}
            """)
    Optional<GetRoleRow> getRoleById(Long id);

    @Select("""
            select exists(
                select 1
                from admin_roles
                where id = #{id}
            )
            """)
    boolean existsRole(Long id);

    @Select("""
            select exists(
                select 1
                from admin_roles
                where id = #{id}
                    and built_in = true
            )
            """)
    boolean isBuiltInRole(Long id);

    // 找出用户的所有角色 code
    @Select("""
            select r.code
            from admin_roles r
            join admin_user_roles ur on ur.role_id = r.id
            join admin_users u on u.id = ur.user_id
            where u.id = #{userId}
                and u.status = 'ACTIVE'
            """)
    List<String> getUserAllRoleCodes(Long userId);

    @Select("""
            select *
            from admin_roles
            """)
    List<GetRoleRow> getAllRoles();

    // 给角色指定权限
    int assignPermissionsToRole(@Param("roleId") Long roleId, @Param("permissionIds") Set<Long> permissionIds);

    @Delete("""
            delete from admin_role_permissions
            where role_id = #{roleId}
            """)
    int clearRolePermissions(Long roleId);

    @Delete("""
            delete from admin_roles
            where id = #{id}
                and built_in = false
            """)
    int deleteRoleById(Long id);

    @Insert("""
            insert into admin_roles(code, name, description)
            values(#{code}, #{name}, #{description})
            """)
    int createRole(CreateRoleMapperParam param);

    @Update("""
            update admin_roles
            set name = #{name},
                description = #{description}
            where id = #{id}
                and built_in = false
            """)
    int updateRole(UpdateRoleMapperParam param);
}
