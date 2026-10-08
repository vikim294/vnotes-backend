package com.vikim.demo.auth;

import com.vikim.demo.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface AuthMapper {

    @Select("""
        select id, username, email, password_hash, avatar_url, created_at, status, token_version
        from users u
        where u.email = #{email}
            and u.status = 'ACTIVE'
    """)
    Optional<User> findUserByEmail(String email);

    @Select("""
        select id, username, email, password_hash, avatar_url, created_at, status, token_version
        from users
        where id = #{id}
    """)
    Optional<User> findUserById(Long id);

    @Select("""
        select status, token_version
        from users
        where id = #{userId}
    """)
    Optional<UserAuthState> findUserAuthStateById(Long userId);

    @Select("""
            select exists(
                select 1
                from users
                where id = #{userId}
                    and status = 'ACTIVE'
            )
            """)
    boolean isActiveUser(Long userId);
}
