package com.vikim.demo.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterAccountRequest(
        @NotBlank(message = "用户名不能为空")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, message = "密码不能为少于 8 位")
        String password,

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱不合法")
        @Size(max = 255, message = "邮箱太长了")
        String email
) {
}
