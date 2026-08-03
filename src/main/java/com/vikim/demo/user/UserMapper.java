package com.vikim.demo.user;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface UserMapper {


    @Select("""
        select id, username, email, created_at
        from users
    """)
    List<User> findAll();


}