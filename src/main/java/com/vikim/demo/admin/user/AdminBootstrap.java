package com.vikim.demo.admin.user;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final InitSuperAdminService initSuperAdminService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 如果没有超级管理员 -> init 一个用户 + 超级管理员角色
        if(initSuperAdminService.hasSuperAdmin()) {
            return;
        }

        initSuperAdminService.initSuperAdmin();
    }
}
