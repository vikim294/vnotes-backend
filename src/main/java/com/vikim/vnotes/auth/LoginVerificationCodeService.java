package com.vikim.vnotes.auth;

import com.vikim.vnotes.common.BusinessException;
import com.vikim.vnotes.common.ErrorCode;
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

    private static final String LOGIN_LIMIT_WINDOW = "60";

    // 先查失败次数 KEYS[1]
    //  存在且 >= 3：return -1 (TOO_MANY_REQUESTS)
    //  不存在：设为 0 并设置 60 秒有效期 ARGV[1]；存在且 < 3
    //      再查 code KEYS[2]
    //          不存在：失败次数 + 1，return 0 (INVALID_EMAIL_OR_VERIFICATION_CODE)
    //          存在但不等 ARGV[2]：失败次数 + 1，return 0 (INVALID_EMAIL_OR_VERIFICATION_CODE)
    //          存在但相等 ARGV[2]：return 1

    private static final DefaultRedisScript<Long> VERIFY_SCRIPT = new DefaultRedisScript<>(
            """
                    local failedTimesValue = redis.call('GET', KEYS[1])
                    local failedTimes = tonumber(failedTimesValue) or 0
                    
                    if failedTimes >= 3 then
                        return -1
                    end
                    
                    if not failedTimesValue then
                       redis.call('SET', KEYS[1], 0, 'EX', ARGV[1])
                    end
                    
                    local code = redis.call('GET', KEYS[2])
                    
                    if not code or code ~= ARGV[2] then
                        redis.call('INCR', KEYS[1])
                        return 0
                    end
                    
                    redis.call('DEL', KEYS[1], KEYS[2])
                    
                    return 1
                    """,
            Long.class
    );

    private String getUserLoginVerificationCodeKey(Long userId) {
        return "user:login:verification-code:" + userId;
    }

    private String getUserLoginLimitKey(Long userId) {
        return "user:login:email-code:fail:" + userId;
    }

    public void saveVerificationCode(Long userId, String verificationCode) {
        String key = getUserLoginVerificationCodeKey(userId);

        stringRedisTemplate.opsForValue()
                .set(key, verificationCode, Duration.ofSeconds(60));
    }

    public void clearVerificationCode(Long userId) {
        String key = getUserLoginVerificationCodeKey(userId);

        stringRedisTemplate.delete(key);
    }

    public void verify(Long userId, String userInputCode) {
        String key1 = getUserLoginLimitKey(userId);
        String key2 = getUserLoginVerificationCodeKey(userId);

        // 原子执行 get + 校验 + 删除
        Long res = stringRedisTemplate.execute(VERIFY_SCRIPT, List.of(key1, key2), LOGIN_LIMIT_WINDOW, userInputCode);

        // 更安全的判断条件写法
        // 即使意外 res 为 null，也不会因为自动拆箱而出现 NullPointerException
        if (Long.valueOf(-1L).equals(res)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }
        else if(!Long.valueOf(1L).equals(res)) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_OR_VERIFICATION_CODE);
        }
    }
}
