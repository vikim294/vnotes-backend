package com.vikim.vnotes.auth;

import com.vikim.vnotes.user.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientUserAuthStateService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String DURATION = "600";

    // 先查 redis，如果存在且版本 >= 本次要更新的版本 -> 放弃更新
    private static final DefaultRedisScript<Long> UPDATE_STATE_SCRIPT = new DefaultRedisScript<>(
            """
                    local tokenVersionValue = redis.call('HGET', KEYS[1], 'tokenVersion')
                    local tokenVersion = tonumber(tokenVersionValue)
                    
                    if tokenVersionValue and tokenVersion >= tonumber(ARGV[2]) then
                        return 0
                    end
                    
                    redis.call('HSET', KEYS[1], 'status', ARGV[1], 'tokenVersion', ARGV[2])
                    redis.call('EXPIRE', KEYS[1], tonumber(ARGV[3]))
                    
                    return 1
                    """,
            Long.class
    );

    private String getUserAuthStateKey(Long userId) {
        return "user:auth:state:" + userId;
    }

    public UserAuthState getUserAuthState(Long userId) {
        Map<Object, Object> map =
                stringRedisTemplate.opsForHash()
                        .entries(getUserAuthStateKey(userId));

        if (map.isEmpty()) {
            return null;
        }

        Object status = map.get("status");
        Object tokenVersion = map.get("tokenVersion");

        if (status == null || tokenVersion == null) {
            return null;
        }

        return new UserAuthState(
                UserStatus.valueOf(status.toString()),
                Long.valueOf(tokenVersion.toString())
        );
    }

    public void setUserAuthState(Long userId, UserAuthState state) {
        // 使用 lua 先检查 tokenVersion 再写入缓存
        stringRedisTemplate.execute(UPDATE_STATE_SCRIPT, List.of(getUserAuthStateKey(userId)), state.status().name(), state.tokenVersion().toString(), DURATION);
    }
}
