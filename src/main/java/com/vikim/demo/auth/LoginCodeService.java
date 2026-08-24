package com.vikim.demo.auth;

import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginCodeService {

    private final StringRedisTemplate stringRedisTemplate;

    private String getKey(String loginCode) {
        return "oauth:login:code:" + loginCode;
    }

    public void cache(String token, String loginCode) {
        String key = getKey(loginCode);

        stringRedisTemplate.opsForValue()
                .set(key, token, Duration.ofSeconds(30));
    }

    public String getTokenByLoginCode(String loginCode) {
        String key = getKey(loginCode);

        // 原子操作 get and delete
        String token = stringRedisTemplate.opsForValue()
                .getAndDelete(key);

        if (token == null) {
            throw new BusinessException(
                    ErrorCode.OAUTH_LOGIN_CODE_INVALID_OR_EXPIRED
            );
        }

        return token;
    }
}
