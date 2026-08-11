package com.vikim.demo.user;


import com.vikim.demo.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    private Long getIdFromAuthentication(
            Authentication authentication
    ) {
        return Long.valueOf(authentication.getName());
    }


    @GetMapping
    public ApiResponse<List<UserResponse>> findAll() {
        return ApiResponse.success(userService.findAll(), "获取用户列表成功");
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> registerAccount(@Valid @RequestBody RegisterAccountRequest request) {
        userService.registerAccount(request);
        return ApiResponse.success("注册成功");
    }

    @GetMapping("/me")
    public ApiResponse<String> me(
            Authentication authentication
    ) {
        return ApiResponse.success(authentication.getName(), "获取当前用户信息成功");
    }

    @PostMapping("/avatar")
    public ApiResponse<UploadAvatarResponse> uploadAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.success(userService.uploadAvatar(getIdFromAuthentication(authentication), file), "上传头像成功");
    }
}