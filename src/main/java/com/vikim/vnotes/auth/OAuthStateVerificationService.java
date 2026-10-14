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
public class OAuthStateVerificationService {
    private final StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> VERIFY_SCRIPT = new DefaultRedisScript<>(
            """
                    local sessionId = redis.call('GET', KEYS[1])
                    
                    if not sessionId or sessionId ~= ARGV[1] then
                        return 0
                    end
                    
                    redis.call('DEL', KEYS[1])
                    
                    return 1
                    """,
            Long.class
    );

    private String getKey(String state) {
        return "oauth-state:" + state;
    }

    public void addState(String state, String sessionId){
        stringRedisTemplate.opsForValue()
                .set(getKey(state), sessionId, Duration.ofSeconds(300));
    }

    public void verifyState(String state, String sessionId){
        Long res = stringRedisTemplate.execute(VERIFY_SCRIPT, List.of(getKey(state)), sessionId);
        if (!Long.valueOf(1L).equals(res)) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_STATE);
        }
    }
}
