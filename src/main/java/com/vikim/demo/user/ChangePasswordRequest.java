package com.vikim.demo.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "旧密码不能为空")
        @Size(min = 8, message = "旧密码不能为少于 8 位")
        String oldPassword,

        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, message = "新密码不能为少于 8 位")
        String newPassword
) {
}
