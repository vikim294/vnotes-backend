package com.vikim.vnotes.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginRateLimitService {
    private final StringRedisTemplate stringRedisTemplate;

    private static final String LOGIN_LIMIT_WINDOW = "60";

    private static final DefaultRedisScript<Long> USER_LOGIN_LIMIT_SCRIPT = new DefaultRedisScript<>(
            """
                    local count = redis.call('INCR', KEYS[1])
                    
                    if count == 1 then
                        redis.call('EXPIRE', KEYS[1], ARGV[1])
                    end
                    
                    return count
                    """,
            Long.class
    );

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

        // 原子执行 incr + expire，避免应用崩溃导致 expire 没设置上
        stringRedisTemplate.execute(USER_LOGIN_LIMIT_SCRIPT, List.of(key), LOGIN_LIMIT_WINDOW);
    }
}
