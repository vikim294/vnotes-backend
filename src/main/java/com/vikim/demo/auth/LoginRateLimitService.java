package com.vikim.demo.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginRateLimitService {
    private final StringRedisTemplate stringRedisTemplate;

    private String getUserLoginLimitKey(Long userId) {
        return "user:login:times:" + userId;
    }

    public boolean hasReachedLimit(Long userId) {
        String key = getUserLoginLimitKey(userId);

        String times = stringRedisTemplate.opsForValue()
                .get(key);

        if (times == null) return false;

        return Long.parseLong(times) >= 3;
    }

    public void recordFailure(Long userId) {
        String key = getUserLoginLimitKey(userId);

        Long newValue = stringRedisTemplate.opsForValue()
                .increment(key);

        if(newValue == 1) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(60));
        }
    }
}
