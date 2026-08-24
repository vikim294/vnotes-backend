package com.vikim.demo.auth;

import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import com.vikim.demo.oauthAccount.OauthAccount;
import com.vikim.demo.oauthAccount.OauthAccountMapper;
import com.vikim.demo.oauthAccount.OauthProvider;
import com.vikim.demo.user.CreateUserForOAuthAccountParam;
import com.vikim.demo.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthTransactionService {
    private final OauthAccountMapper oauthAccountMapper;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final LoginCodeService loginCodeService;

    @Transactional
    public Long googleCallback(GoogleUserInfo userInfo){
        // 判断 oauth_accounts 中是否已有该用户
        Optional<OauthAccount> optionalOauthAccount = oauthAccountMapper.findByGoogleSub(userInfo.sub());
        if(optionalOauthAccount.isPresent()) {
            // 已有该用户 -> 生成 jwt
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

            return param.getId();
        }
    }

    public void generateTokenByUserIdAndCacheWithLoginCode(Long userId, String loginCode) {
        // 生成 jwt
        LoginResponse loginResponse = jwtService.generateAccessToken(userId);
        // 暂存 redis
        loginCodeService.cache(loginResponse.accessToken(), loginCode);
    }
}
