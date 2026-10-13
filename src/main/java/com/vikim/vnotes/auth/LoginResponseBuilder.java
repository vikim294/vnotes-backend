package com.vikim.vnotes.auth;

import com.vikim.vnotes.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class LoginResponseBuilder {
    @Value("${app.cookie.secure:false}")
    boolean cookieSecure;

    public ResponseEntity<ApiResponse<LoginResponse>> build(LoginResponse response){
        ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.ok();

        // 设置响应头 set-cookie
        ResponseCookie cookie = ResponseCookie
                .from("gray_bucket", String.valueOf(response.userId() % 100))
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(365))
                .build();

        bodyBuilder.header(HttpHeaders.SET_COOKIE, cookie.toString());

        return bodyBuilder.body(ApiResponse.success(response, "登录成功"));
    }
}
