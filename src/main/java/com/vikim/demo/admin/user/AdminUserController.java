package com.vikim.demo.admin.user;

import com.vikim.demo.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
