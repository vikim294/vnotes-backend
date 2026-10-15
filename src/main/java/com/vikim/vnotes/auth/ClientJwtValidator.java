package com.vikim.vnotes.auth;

import com.vikim.vnotes.user.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientJwtValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_TOKEN =
            new OAuth2Error(
                    "invalid_token",
                    "Token is no longer valid",
                    null
            );

    private final AuthMapper authMapper;
    private final ClientUserAuthStateService clientUserAuthStateService;

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

        // 先查 redis cache aside，如果没有缓存，才查数据库，并建立 redis 缓存
        // 更新时（启用/禁用客户端用户）需要删除 redis 缓存
        // ⚠️但存在并发问题：查数据库后得到 ACTIVE，建立缓存之前，如果用户被 DISABLE 了，就会把 ACTIVE 写入缓存，10 mins 才过期
        // 根本原因是 查数据库后建缓存 不是一个原子操作，所以建缓存之前，请求可能操作数据库，导致旧状态写入缓存
        // 解决办法不是把它们变成一个原子操作，而是：
        // 更新时，不删除缓存，而是更新缓存，并且要保证 tokenVersion > 已有缓存的 tokenVersion
        // 这样，“迟到的缓存建立”不会把旧状态写入缓存
        UserAuthState state = null;
        boolean badRedis = false;

        try {
            state = clientUserAuthStateService.getUserAuthState(userId);
        } catch (RedisConnectionFailureException | QueryTimeoutException e) {
            // redis 故障，降级到数据库
            log.warn("Redis unavailable, fallback to database", e);
            badRedis = true;
        }

        if (state == null) {
            // 查数据库
            state = authMapper.findUserAuthStateById(userId).orElse(null);
            if(state != null && !badRedis) {
                // 建立 redis 缓存
                try {
                    clientUserAuthStateService.setUserAuthState(userId, state);
                }
                catch (RedisConnectionFailureException | QueryTimeoutException e) {
                    log.warn("Redis unavailable, fallback to database", e);
                }
            }
        }

        if (state == null
                || state.status() != UserStatus.ACTIVE
                || state.tokenVersion().longValue() != tokenVersion.longValue()) {
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