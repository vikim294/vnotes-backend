package com.vikim.vnotes.admin.clientuser;

import com.vikim.vnotes.auth.ClientUserAuthStateService;
import com.vikim.vnotes.auth.UserAuthState;
import com.vikim.vnotes.objectStorage.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientUserService {

    private final ClientUserMapper clientUserMapper;
    private final ObjectStorageService objectStorageService;
    private final ClientUserTransactionService clientUserTransactionService;
    private final ClientUserAuthStateService clientUserAuthStateService;

    @PreAuthorize("hasAuthority('client:user:read')")
    public List<GetClientUserResponse> getClientUserList(){
        List<GetClientUserRow> rows = clientUserMapper.getClientUserList();

        return rows.stream()
                .map(row -> new GetClientUserResponse(
                        row.id(),
                        row.username(),
                        row.email(),
                        objectStorageService.generatePresignedUrl(row.avatarUrl()),
                        row.status(),
                        row.noteCount(),
                        row.sharedNoteCount(),
                        row.articleCount(),
                        row.createdAt().toInstant(ZoneOffset.UTC)
                ))
                .toList();
    }

    public void enableOrDisableClientUser(
            Long operatorId,
            EnableOrDisableClientUserRequest request
    ){
        UserAuthState state = clientUserTransactionService.enableOrDisableClientUser(operatorId, request);
        if(state != null) {
            try {
                // 更新 redis 缓存
                clientUserAuthStateService.setUserAuthState(request.userId(), state);
            } catch (RedisConnectionFailureException | QueryTimeoutException e) {
                // redis 故障
                log.error("Failed to update client auth state cache, userId={}",
                        request.userId(), e);
            }
        }
    }
}
