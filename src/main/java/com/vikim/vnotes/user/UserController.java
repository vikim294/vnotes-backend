package com.vikim.vnotes.user;


import com.vikim.vnotes.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getProfile(
            Authentication authentication
    ) {
        return ApiResponse.success(userService.getProfile(getIdFromAuthentication(authentication)), "获取当前用户信息成功");
    }

    @PostMapping("/avatar")
    public ApiResponse<UploadAvatarResponse> uploadAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        UploadAvatarResponse response = userService.uploadAvatarToObjectStorage(getIdFromAuthentication(authentication), file);
        return ApiResponse.success(response, "上传头像成功");
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(getIdFromAuthentication(authentication), request.oldPassword(), request.newPassword());
        return ApiResponse.success("修改密码成功");
    }
}