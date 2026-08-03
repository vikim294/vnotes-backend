package com.vikim.demo.user;


import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserService {


    private final UserMapper userMapper;


    public UserService(UserMapper userMapper) {

        this.userMapper = userMapper;

    }


    public List<UserResponse> findAll(){

        return userMapper.findAll().stream()
                .map(UserResponse::from)
                .toList();

    }

}