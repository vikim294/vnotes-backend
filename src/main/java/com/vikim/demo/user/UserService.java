package com.vikim.demo.user;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;


@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<UserResponse> findAll() {
        return userMapper.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    public void registerAccount(RegisterAccountRequest request) {
        Optional<User> user = userMapper.findByEmail(request.email());
        if(user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户邮箱已存在");
        }

        int res = userMapper.registerAccount(request.username(), request.password(), request.email());
        if (res == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "注册账号失败");
        }
    }
}