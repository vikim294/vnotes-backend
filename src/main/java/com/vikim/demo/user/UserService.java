package com.vikim.demo.user;


import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import org.springframework.stereotype.Service;

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
        if (user.isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        userMapper.registerAccount(request.username(), request.password(), request.email());
    }
}