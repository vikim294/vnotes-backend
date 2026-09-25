package com.vikim.demo.admin.user;

import com.vikim.demo.admin.auth.GetUserIdFromAuthentication;
import com.vikim.demo.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/user")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/list")
    public ApiResponse<List<GetUserListResponse>> getUserList(){
        return ApiResponse.success(adminUserService.getUserList(), "获取用户列表成功");
    }

    @GetMapping("/list-with-roles")
    public ApiResponse<List<GetUserWithRolesListResponse>> getUserWithRolesList(){
        return ApiResponse.success(adminUserService.getUserWithRolesList(), "获取用户列表成功");
    }

    @PostMapping("/add")
    public ApiResponse<Void> addUser(
            Authentication authentication,
            @Valid @RequestBody CreateAdminUserRequest request
    ) {
        adminUserService.addUser(GetUserIdFromAuthentication.getIdFromAuthentication(authentication), request);
        return ApiResponse.success("新增用户成功");
    }

    @PatchMapping("/enable-or-disable")
    public ApiResponse<Void> enableOrDisableUser(
            Authentication authentication,
            @Valid @RequestBody EnableOrDisableUserRequest request
    ){
        adminUserService.enableOrDisableUser(GetUserIdFromAuthentication.getIdFromAuthentication(authentication), request);
        return ApiResponse.success(request.status() == AdminUserStatus.ACTIVE ? "启用用户成功" : "禁用用户成功");
    }

    @PostMapping("/assign-roles")
    public ApiResponse<Void> assignRolesToUser(
            Authentication authentication,
            @Valid @RequestBody AssignRolesToUserRequest request
    ){
        adminUserService.assignRolesToUser(GetUserIdFromAuthentication.getIdFromAuthentication(authentication), request);
        return ApiResponse.success("为用户指定角色成功");
    }
}
