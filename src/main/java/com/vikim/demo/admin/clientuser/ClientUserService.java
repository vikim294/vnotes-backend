package com.vikim.demo.admin.clientuser;

import com.vikim.demo.admin.audit.AdminAuditAction;
import com.vikim.demo.admin.audit.AdminAuditService;
import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import com.vikim.demo.objectStorage.ObjectStorageService;
import com.vikim.demo.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientUserService {

    private final ClientUserMapper clientUserMapper;
    private final ObjectStorageService objectStorageService;
    private final AdminAuditService adminAuditService;

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

    @PreAuthorize("hasAuthority('client:user:enable_and_disable')")
    @Transactional
    // todo 禁用只能阻止新登录。如果要立即生效，需要在请求鉴权链中检查状态，或引入 tokenVrsion/Redis 撤销机制
    public void enableOrDisableClientUser(
            Long operatorId,
            EnableOrDisableClientUserRequest request
    ){
        if(clientUserMapper.enableOrDisableClientUser(request.userId(), request.status()) != 1) {
            // 说明 where 匹配到 0 行

            // 如果用户不存在
            if(!clientUserMapper.existsById(request.userId())) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }

            // 如果用户是存在的，说明该操作是重复启用/禁用，无需记录日志
            return;
        }

        adminAuditService.createAuditLog(
                operatorId,
                request.status() == UserStatus.ACTIVE ? AdminAuditAction.ENABLE_CLIENT_USER : AdminAuditAction.DISABLE_CLIENT_USER,
                (request.status() == UserStatus.ACTIVE ? "启用" : "禁用") + "客户端用户：" + request.userId()
        );
    }
}
