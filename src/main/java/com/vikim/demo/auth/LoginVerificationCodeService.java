package com.vikim.demo.auth;

import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginVerificationCodeService {
    private final StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> VERIFY_SCRIPT = new DefaultRedisScript<>(
            """
                    local code = redis.call('GET', KEYS[1])
                    
                    if not code or code ~= ARGV[1] then
                        return 0
                    end
                    
                    redis.call('DEL', KEYS[1])
                    
                    return 1
                    """,
            Long.class
    );

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

    public void verify(Long userId, String userInputCode) {
        String key = getUserLoginVerificationCodeKey(userId);

        // 原子执行 get + 校验 + 删除
        Long res = stringRedisTemplate.execute(VERIFY_SCRIPT, List.of(key), userInputCode);

        // 更安全的判断条件写法
        // 即使意外 res 为 null，也不会因为自动拆箱而出现 NullPointerException
        if (!Long.valueOf(1L).equals(res)) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_OR_VERIFICATION_CODE);
        }
    }
}
