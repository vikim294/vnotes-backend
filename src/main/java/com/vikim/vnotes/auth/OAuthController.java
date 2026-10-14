package com.vikim.vnotes.auth;

import com.vikim.vnotes.common.ApiResponse;
import com.vikim.vnotes.common.BusinessException;
import com.vikim.vnotes.common.ErrorCode;
import com.vikim.vnotes.user.User;
import com.vikim.vnotes.user.UserStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
    private final OAuthStateVerificationService oAuthStateVerificationService;

    @Value("${app.cookie.secure:false}")
    boolean cookieSecure;

    private String encodeUri(String uri) {
        return URLEncoder.encode(uri, StandardCharsets.UTF_8);
    }

    // 生成并重定向用户浏览器到 google 授权 url
    @GetMapping("/google/login")
    public ResponseEntity<Void> googleLogin() {
        // 生成 state 参数，放入 url 和 redis
        // state 和 cookie 用于往返验证
        // 在后面的 /google/callback 接口中校验，验证回调请求与当前浏览器发起的授权请求 来自同一个浏览器，防 CSRF
        String state = UUID.randomUUID().toString();

        String url = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + oAuthProperties.googleClientId() +
                // 手动拼接 url 需自己 encode
                "&redirect_uri=" + encodeUri(oAuthProperties.googleRedirectUri()) +
                "&response_type=" + "code" +
                // 手动拼接 url 需自己 encode
                "&scope=" + encodeUri("openid email profile") +
                "&state=" + encodeUri(state);

        // 生成 sessionId
        String sessionId = UUID.randomUUID().toString();

        // state 和 sessionId 放入 redis
        oAuthStateVerificationService.addState(state, sessionId);

        // 把 sessionId 放入 cookie
        ResponseCookie cookie = ResponseCookie
                .from("oauth_session_id", sessionId)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/oauth/google")
                // 显式指定 Lax，保证跨站 google 回调 /google/callback 时携带 cookie
                .sameSite("Lax")
                .maxAge(Duration.ofMinutes(5))
                .build();

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(url))
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    // 用户在 google 授权页面完成授权后，浏览器会访问这个接口
    // 攻击者可以先在自己的浏览器中完成授权 截住包含自己的 code 和 state 的回调地址，然后诱导受害者在浏览器中直接访问
    // 从而使受害者登录 攻击者的 google 账号相关联的 vnotes 账号，导致受害者把敏感数据写进错误账号
    // 所以关键是要区分攻击者和受害者的浏览器，或者说 保证授权请求和回调来自同一个浏览器
    // 可以在生成 state 的同时生成 cookie 并存入 redis，然后设置 cookie 响应
    // 在 /google/callback 中同时校验 state 和 cookie
    @GetMapping("/google/callback")
    public ResponseEntity<Void> googleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @CookieValue(value = "oauth_session_id")
            String sessionId
    ) {
        // 校验 state 和 cookie
        oAuthStateVerificationService.verifyState(state, sessionId);

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

        // 清理消费了的 cookie
        ResponseCookie expiredCookie = ResponseCookie
                .from("oauth_session_id", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/oauth/google")
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(oAuthProperties.loginUrl() + "?loginCode=" + loginCode))
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
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
