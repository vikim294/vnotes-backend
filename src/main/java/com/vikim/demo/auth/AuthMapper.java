package com.vikim.demo.auth;

import com.vikim.demo.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface AuthMapper {

    @Select("""
        select id, username, email, password_hash
        from users u
        where u.email = #{email}
    """)
    Optional<User> findUserByEmail(String email);
}
