package com.vikim.demo.notification;

import com.vikim.demo.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private Long getIdFromAuthentication(
            Authentication authentication
    ) {
        return Long.valueOf(authentication.getName());
    }

    @GetMapping
    public ApiResponse<List<FetchUserNotificationsResponse>> fetchUserNotifications(
            Authentication authentication
    ){
        return ApiResponse.success(notificationService.fetchUserNotifications(getIdFromAuthentication(authentication)), "获取通知成功");
    }

}
