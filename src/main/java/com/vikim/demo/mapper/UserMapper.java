package com.vikim.demo.mapper;


import com.vikim.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface UserMapper {


    @Select("""
        select *
        from users
    """)
    List<User> findAll();


}