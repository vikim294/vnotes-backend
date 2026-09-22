package com.vikim.demo.admin.auth;

import com.vikim.demo.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ){
        return ApiResponse.success(adminAuthService.login(request), "登录成功");
    }
}
