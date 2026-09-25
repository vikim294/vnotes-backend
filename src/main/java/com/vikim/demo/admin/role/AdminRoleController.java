package com.vikim.demo.admin.role;

import com.vikim.demo.admin.auth.GetUserIdFromAuthentication;
import com.vikim.demo.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/role")
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    @GetMapping("/list")
    public ApiResponse<List<GetRoleResponse>> getAllRoles(){
        return ApiResponse.success(adminRoleService.getAllRoles(), "获取所有角色成功");
    }


    @GetMapping("/list-with-permissions")
    public ApiResponse<List<GetRoleWithPermissionResponse>> getAllRolesWithPermissions(){
        return ApiResponse.success(adminRoleService.getAllRolesWithPermissions(), "获取所有角色成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRoleById(
            Authentication authentication,
            @PathVariable("id") Long id
    ){
        adminRoleService.deleteRoleById(GetUserIdFromAuthentication.getIdFromAuthentication(authentication), id);
        return ApiResponse.success("删除角色成功");
    }

    @PostMapping
    public ApiResponse<Void> createRole(
            Authentication authentication,
            @Valid @RequestBody CreateRoleRequest request
    ){
        adminRoleService.createRole(GetUserIdFromAuthentication.getIdFromAuthentication(authentication), request);
        return ApiResponse.success("创建角色成功");
    }

    @PatchMapping
    public ApiResponse<Void> updateRole(
            Authentication authentication,
            @Valid @RequestBody UpdateRoleRequest request
    ){
        adminRoleService.updateRole(GetUserIdFromAuthentication.getIdFromAuthentication(authentication), request);
        return ApiResponse.success("更新角色成功");
    }

    @PostMapping("/assign-permissions")
    public ApiResponse<Void> assignPermissionsToRole(
            Authentication authentication,
            @Valid @RequestBody AssignPermissionsToRoleRequest request
    ){
        adminRoleService.assignPermissionsToRole(GetUserIdFromAuthentication.getIdFromAuthentication(authentication), request);
        return ApiResponse.success("为角色指定权限成功");
    }
}
