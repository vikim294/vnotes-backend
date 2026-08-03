package com.vikim.demo.user;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;


@Mapper
public interface UserMapper {

    @Select("""
        select id, username, email, created_at
        from users
    """)
    List<User> findAll();

    @Insert("""
        insert into users(username, password, email)
        values(#{username}, #{password}, #{email})
    """)
    int registerAccount(String username, String password, String email);

    @Select("""
            select id, username
            from users u
            where u.email = #{email}
            """)
    Optional<User> findByEmail(String email);
}