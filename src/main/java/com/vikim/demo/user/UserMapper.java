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
    int registerAccount(String username, String passwordHash, String email);

    @Select("""
            select id, username
            from users u
            where u.email = #{email}
            """)
    Optional<User> findByEmail(String email);

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
}