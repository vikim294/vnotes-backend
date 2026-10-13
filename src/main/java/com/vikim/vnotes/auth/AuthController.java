package com.vikim.vnotes.auth;

import com.vikim.vnotes.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LoginResponseBuilder loginResponseBuilder;

    @PostMapping("/login/email-password")
    public ResponseEntity<ApiResponse<LoginResponse>> loginByEmailAndPassword(
            @Valid @RequestBody LoginByEmailAndPasswordRequest request
    ) {
        LoginResponse response = authService.loginByEmailAndPassword(request);
        return loginResponseBuilder.build(response);
    }

    @PostMapping("/send-verification-code")
    public ApiResponse<Void> sendVerificationCode(
            @Valid @RequestBody SendVerificationCodeRequest request
    ) {
        authService.sendVerificationCode(request);
        return ApiResponse.success("发送验证码成功");
    }

    @PostMapping("/login/email-verification-code")
    public ResponseEntity<ApiResponse<LoginResponse>> loginByEmailAndVerificationCode(
            @Valid @RequestBody LoginByEmailAndVerificationCodeRequest request
    ) {
        LoginResponse response = authService.loginByEmailAndVerificationCode(request);
        return loginResponseBuilder.build(response);
    }
}
