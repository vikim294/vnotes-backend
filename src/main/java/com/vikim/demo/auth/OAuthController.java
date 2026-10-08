package com.vikim.demo.auth;

import com.vikim.demo.common.ApiResponse;
import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import com.vikim.demo.user.User;
import com.vikim.demo.user.UserStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthGoogleService oAuthGoogleService;
    private final OAuthProperties oAuthProperties;
    private final OAuthTransactionService oAuthTransactionService;
    private final LoginCodeService loginCodeService;
    private final JwtService jwtService;
    private final AuthService authService;
    private final LoginResponseBuilder loginResponseBuilder;

    private String encodeUri(String uri) {
        return URLEncoder.encode(uri, StandardCharsets.UTF_8);
    }

    @GetMapping("/google/login")
    public ResponseEntity<Void> googleLogin() {
        String url = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + oAuthProperties.googleClientId() +
                // 手动拼接 url 需自己 encode
                "&redirect_uri=" + encodeUri(oAuthProperties.googleRedirectUri()) +
                "&response_type=" + "code" +
                // 手动拼接 url 需自己 encode
                "&scope=" + encodeUri("openid email profile");

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }

    @GetMapping("/google/callback")
    public ResponseEntity<Void> googleCallback(
            @RequestParam("code") String code
    ) {
        // 用 code 换 Google 用户信息
        GetGoogleTokenResponse res = oAuthGoogleService.getGoogleTokenByCode(code);
        // 验证 + 解析 idToken
        GoogleUserInfo userInfo = oAuthGoogleService.verifyAndParseIdToken(res.getId_token());
        // 得到 userId
        Long userId = oAuthTransactionService.googleCallback(userInfo);

        // 生成临时 loginCode
        String loginCode = UUID.randomUUID().toString();

        // 暂存 redis 操作放到事务返回后
        loginCodeService.cache(userId, loginCode);

        // 带上 login code 重定向到登录页
        // 本地测试 http://localhost:5173/login?loginCode=
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(oAuthProperties.loginUrl() + "?loginCode=" + loginCode))
                .build();
    }

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<LoginResponse>> getTokenByLoginCode(
            @Valid @RequestBody GetTokenByLoginCodeRequest request
    ) {
        Long userId = loginCodeService.getUserIdByLoginCode(request.code());
        User user = authService.findUserById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 判断用户 status
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.USER_IS_DISABLED);
        }

        LoginResponse response = jwtService.generateAccessToken(userId, user.getTokenVersion());
        return loginResponseBuilder.build(response);
    }
}
