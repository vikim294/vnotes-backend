package com.vikim.demo.admin.role;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AdminRoleMapper {

    @Select("""
            select id
            from admin_roles
            where code = #{code}
            """)
    Optional<Long> getIdByCode(String code);

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
}
