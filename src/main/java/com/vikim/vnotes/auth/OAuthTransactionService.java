package com.vikim.vnotes.auth;

import com.vikim.vnotes.common.BusinessException;
import com.vikim.vnotes.common.ErrorCode;
import com.vikim.vnotes.oauthAccount.OauthAccount;
import com.vikim.vnotes.oauthAccount.OauthAccountMapper;
import com.vikim.vnotes.oauthAccount.OauthProvider;
import com.vikim.vnotes.user.CreateUserForOAuthAccountParam;
import com.vikim.vnotes.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthTransactionService {
    private final OauthAccountMapper oauthAccountMapper;
    private final UserMapper userMapper;

    @Transactional
    public Long googleCallback(GoogleUserInfo userInfo){
        // 判断 oauth_accounts 中是否已有该用户
        Optional<OauthAccount> optionalOauthAccount = oauthAccountMapper.findByGoogleSub(userInfo.sub());
        if(optionalOauthAccount.isPresent()) {
            // 已有该用户 -> 直接返回 userId
            return optionalOauthAccount.get().getUserId();
        }
        else {
            // 没有该用户 -> 创建 user
            CreateUserForOAuthAccountParam param = new CreateUserForOAuthAccountParam();
            if(userMapper.createUserForOAuthAccount(param) != 1) {
                throw new BusinessException(ErrorCode.USER_CREATED_FAILED);
            }
            // 创建 oauth account 并关联
            if(oauthAccountMapper.createGoogleOAuthAccount(param.getId(), OauthProvider.GOOGLE, userInfo.sub()) != 1) {
                throw new BusinessException(ErrorCode.OAUTH_ACCOUNT_CREATED_FAILED);
            }

            // 返回 userId
            return param.getId();
        }
    }
}
