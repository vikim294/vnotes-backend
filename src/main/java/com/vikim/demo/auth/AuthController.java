package com.vikim.demo.auth;

import com.vikim.demo.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/email-password")
    public ApiResponse<LoginResponse> loginByEmailAndPassword(
            @Valid @RequestBody LoginByEmailAndPasswordRequest request
    ) {
        return ApiResponse.success(authService.loginByEmailAndPassword(request), "登录成功");
    }

    @PostMapping("/send-verification-code")
    public ApiResponse<Void> sendVerificationCode(
            @Valid @RequestBody SendVerificationCodeRequest request
    ) {
        authService.sendVerificationCode(request);
        return ApiResponse.success("发送验证码成功");
    }

    @PostMapping("/login/email-verification-code")
    public ApiResponse<LoginResponse> loginByEmailAndVerificationCode(
            @Valid @RequestBody LoginByEmailAndVerificationCodeRequest request
    ) {
        return ApiResponse.success(authService.loginByEmailAndVerificationCode(request), "登录成功");
    }
}
