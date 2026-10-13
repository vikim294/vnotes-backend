package com.vikim.vnotes.admin.permission;

import com.vikim.vnotes.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/permission")
public class AdminPermissionController {

    private final AdminPermissionService adminPermissionService;

    @GetMapping("/list")
    public ApiResponse<List<GetPermissionResponse>> getAllPermissions(){
        return ApiResponse.success(adminPermissionService.getAllPermissions(), "获取所有权限成功");
    }

}
