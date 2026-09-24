package com.vikim.demo.admin.audit;

import com.vikim.demo.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/audit")
public class AdminAuditController {

    private final AdminAuditService adminAuditService;

    @GetMapping("/list")
    public ApiResponse<List<GetAuditLogResponse>> getAuditLogs(){
        return ApiResponse.success(adminAuditService.getAuditLogs(), "获取审计日志成功");
    }

}
