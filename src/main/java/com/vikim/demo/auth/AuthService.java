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
    private final LoginRateLimitService loginRateLimitService;


    public AuthService(AuthMapper authMapper, PasswordEncoder passwordEncoder, JwtService jwtService, LoginRateLimitService loginRateLimitService) {
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginRateLimitService = loginRateLimitService;
    }

    private Optional<User> findUserByEmail(String email) {
        return authMapper.findUserByEmail(email);
    }

    public LoginResponse login(LoginRequest request) {
        User user = findUserByEmail(request.email()).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 先查 redis 登录限流
        if(loginRateLimitService.hasReachedLimit(user.getId())) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }

        // 校验密码
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            // 更新失败次数
            loginRateLimitService.recordFailure(user.getId());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 生成并返回 token
        return jwtService.generateAccessToken(user);
    }
}
