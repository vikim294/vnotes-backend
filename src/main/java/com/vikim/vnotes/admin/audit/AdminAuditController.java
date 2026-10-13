package com.vikim.vnotes.admin.audit;

import com.vikim.vnotes.admin.auth.GetUserIdFromAuthentication;
import com.vikim.vnotes.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/audit")
public class AdminAuditController {

    private final AdminAuditService adminAuditService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @GetMapping("/list")
    public ApiResponse<List<GetAuditLogResponse>> getAuditLogs() {
        return ApiResponse.success(adminAuditService.getAuditLogs(), "获取审计日志成功");
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportAuditLogsToExcel(
            Authentication authentication
    ) {
        byte[] bytes = adminAuditService.exportAuditLogsToExcel(GetUserIdFromAuthentication.getIdFromAuthentication(authentication));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-logs-" + ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).format(FORMATTER) + ".xlsx\"")
                .body(bytes);
    }

}
