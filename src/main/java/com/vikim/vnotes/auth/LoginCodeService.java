package com.vikim.vnotes.auth;

import com.vikim.vnotes.common.BusinessException;
import com.vikim.vnotes.common.ErrorCode;
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

    // 缓存用户的 loginCode
    public void cache(Long userId, String loginCode) {
        String key = getKey(loginCode);

        stringRedisTemplate.opsForValue()
                .set(key, userId.toString(), Duration.ofSeconds(30));
    }

    public Long getUserIdByLoginCode(String loginCode) {
        String key = getKey(loginCode);

        String userId = stringRedisTemplate.opsForValue().getAndDelete(key);

        if(userId == null) {
            throw new BusinessException(ErrorCode.OAUTH_LOGIN_CODE_INVALID_OR_EXPIRED);
        }

        return Long.valueOf(userId);
    }
}
