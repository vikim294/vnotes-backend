package com.vikim.demo.auth;

import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import com.vikim.demo.user.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public AuthService(AuthMapper authMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    private Optional<User> findUserByEmail(String email) {
        return authMapper.findUserByEmail(email);
    }

    public LoginResponse login(LoginRequest request) {
        User user = findUserByEmail(request.email()).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 校验密码
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 生成并返回 token
        return jwtService.generateAccessToken(user);
    }
}
