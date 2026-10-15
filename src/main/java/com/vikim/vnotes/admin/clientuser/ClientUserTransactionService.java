package com.vikim.vnotes.admin.clientuser;

import com.vikim.vnotes.admin.audit.AdminAuditAction;
import com.vikim.vnotes.admin.audit.AdminAuditService;
import com.vikim.vnotes.auth.AuthMapper;
import com.vikim.vnotes.auth.UserAuthState;
import com.vikim.vnotes.common.BusinessException;
import com.vikim.vnotes.common.ErrorCode;
import com.vikim.vnotes.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientUserTransactionService {

    private final ClientUserMapper clientUserMapper;
    private final AdminAuditService adminAuditService;
    private final AuthMapper authMapper;

    @PreAuthorize("hasAuthority('client:user:enable_and_disable')")
    @Transactional
    public UserAuthState enableOrDisableClientUser(
            Long operatorId,
            EnableOrDisableClientUserRequest request
    ){
        int affectedRows = 0;

        if(request.status() == UserStatus.DISABLED) {
            affectedRows = clientUserMapper.disableClientUser(request.userId());
        }
        else {
            affectedRows = clientUserMapper.enableClientUser(request.userId());
        }

        if(affectedRows != 1) {
            // 说明 where 匹配到 0 行

            // 如果用户不存在
            if(!clientUserMapper.existsById(request.userId())) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }

            // 如果用户是存在的，说明该操作是重复启用/禁用，无需记录日志
            return null;
        }

        adminAuditService.createAuditLog(
                operatorId,
                request.status() == UserStatus.ACTIVE ? AdminAuditAction.ENABLE_CLIENT_USER : AdminAuditAction.DISABLE_CLIENT_USER,
                (request.status() == UserStatus.ACTIVE ? "启用" : "禁用") + "客户端用户：" + request.userId()
        );

        // 查最新 auth state
        return authMapper.findUserAuthStateById(request.userId()).orElse(null);
    }
}
