package com.vikim.demo.auth;

import com.vikim.demo.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientJwtValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_TOKEN =
            new OAuth2Error(
                    "invalid_token",
                    "Token is no longer valid",
                    null
            );

    private final AuthMapper authMapper;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        // 解析 jwt 自定义 claims 得到 tokenType、userId、tokenVersion，
        // 再查用户表，得到 status 和 tokenVersion
        // 只有 status 为 active 并且两个 tokenVersion 一样才放行，否则 401
        // 全局异常处理在 security filter chain 后面，所以 401 会交给 RestAuthenticationEntryPoint，而不是全局异常处理

        String tokenType =
                jwt.getClaimAsString("tokenType");

        // ADMIN token 暂时交给 admin 自己的校验逻辑
        if ("ADMIN".equals(tokenType)) {
            return OAuth2TokenValidatorResult.success();
        }

        // CLIENT 以外的 token 不接受
        if (!"CLIENT".equals(tokenType)) {
            return failure();
        }

        Long userId;

        try {
            userId = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            return failure();
        }

        Object tokenVersionClaim =
                jwt.getClaim("tokenVersion");

        if (!(tokenVersionClaim instanceof Number tokenVersion)) {
            return failure();
        }

        UserAuthState state =
                authMapper.findUserAuthStateById(userId)
                        .orElse(null);

        if (state == null
                || state.status() != UserStatus.ACTIVE
                || state.tokenVersion().longValue() != tokenVersion.longValue())
        {
            return failure();
        }

        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult failure() {
        return OAuth2TokenValidatorResult.failure(
                INVALID_TOKEN
        );
    }
}