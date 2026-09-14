package com.vikim.demo.user;


import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;


@Mapper
public interface UserMapper {

    @Select("""
        select id, username, email, avatar_url, created_at
        from users
    """)
    List<User> findAll();

    @Insert("""
        insert into users(username, password_hash, email)
        values(#{username}, #{passwordHash}, #{email})
    """)
    @Options(
            useGeneratedKeys = true,
            keyProperty = "id"
    )
    int registerAccount(RegisterAccountMapperParam param);

    @Select("""
            select id, username
            from users u
            where u.email = #{email}
            """)
    Optional<User> findByEmail(String email);

    @Select("""
        select id, username, email, password_hash
        from users
        where id = #{id}
    """)
    Optional<User> findById(Long id);

    @Update("""
            update users
            set avatar_url = #{avatarUrl}
            where id = #{userId}
            """)
    int updateAvatarUrl(@Param("userId") Long userId, @Param("avatarUrl") String avatarUrl);

    @Select("""
            select id, username, email, avatar_url, created_at
            from users
            where id = #{userId}
            """)
    Optional<UserProfileResponse> getProfile(@Param("userId") Long userId);

    @Insert("""
            insert into users(username)
            values('new user')
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int createUserForOAuthAccount(CreateUserForOAuthAccountParam param);

    // 更新时携带原哈希，避免并发请求时 可能覆盖更新（有点类似乐观锁 version）
    @Update("""
            update users
            set password_hash = #{newPasswordHash}
            where id = #{userId}
                and password_hash = #{oldPasswordHash}
            """)
    int changePassword(@Param("userId") Long userId, @Param("oldPasswordHash") String oldPasswordHash, @Param("newPasswordHash") String newPasswordHash);
}