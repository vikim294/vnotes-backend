package com.vikim.demo.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginVerificationCodeService {
    private final StringRedisTemplate stringRedisTemplate;

    private String getUserLoginVerificationCodeKey(Long userId) {
        return "user:login:verification-code:" + userId;
    }

    public void saveVerificationCode(Long userId, String verificationCode) {
        String key = getUserLoginVerificationCodeKey(userId);

        stringRedisTemplate.opsForValue()
                .set(key, verificationCode, Duration.ofSeconds(60));
    }

    public String getVerificationCode(Long userId) {
        String key = getUserLoginVerificationCodeKey(userId);

        return stringRedisTemplate.opsForValue()
                .get(key);
    }

    public void clearVerificationCode(Long userId) {
        String key = getUserLoginVerificationCodeKey(userId);

        stringRedisTemplate.delete(key);
    }
}
