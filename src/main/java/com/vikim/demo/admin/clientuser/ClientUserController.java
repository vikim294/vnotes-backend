package com.vikim.demo.admin.clientuser;

import com.vikim.demo.admin.auth.GetUserIdFromAuthentication;
import com.vikim.demo.common.ApiResponse;
import com.vikim.demo.user.UserStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/client-user")
public class ClientUserController {

    private final ClientUserService clientUserService;

    @GetMapping("/list")
    public ApiResponse<List<GetClientUserResponse>> getClientUserList(){
        return ApiResponse.success(clientUserService.getClientUserList(), "获取客户端用户成功");
    }

    @PatchMapping("/enable-or-disable")
    public ApiResponse<Void> enableOrDisableClientUser(
            Authentication authentication,
            @Valid @RequestBody EnableOrDisableClientUserRequest request
    ){
        clientUserService.enableOrDisableClientUser(GetUserIdFromAuthentication.getIdFromAuthentication(authentication), request);
        return ApiResponse.success(request.status() == UserStatus.ACTIVE ? "启用客户端用户成功" : "禁用客户端用户成功");
    }
}
