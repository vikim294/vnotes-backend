package com.vikim.demo.admin.auth;

import com.vikim.demo.admin.permission.AdminPermissionMapper;
import com.vikim.demo.admin.role.AdminRoleMapper;
import com.vikim.demo.admin.user.AdminUserMapper;
import com.vikim.demo.admin.user.GetUserResponse;
import com.vikim.demo.auth.JwtService;
import com.vikim.demo.auth.LoginResponse;
import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {
    private final AdminUserMapper adminUserMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final AdminPermissionMapper adminPermissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminLoginResponse login(LoginRequest request) {
        GetUserResponse user = adminUserMapper.getActiveUserByEmail(request.email()).orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_INVALID_EMAIL_OR_PASSWORD));

        // 校验密码
        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new BusinessException(ErrorCode.ADMIN_INVALID_EMAIL_OR_PASSWORD);
        }

        // 查用户的 角色、权限
        List<String> userRoleCodes = adminRoleMapper.getUserAllRoleCodes(user.id());
        List<String> userPermissionCodes = adminPermissionMapper.getUserAllPermissionCodes(user.id());

        // 签发 admin 后台管理系统 jwt
        LoginResponse response = jwtService.generateAccessToken(user.id(), userRoleCodes, userPermissionCodes);
        return new AdminLoginResponse(
                response.accessToken(),
                response.expiresIn(),
                userRoleCodes,
                userPermissionCodes
        );
    }
}
