package com.vikim.demo.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTagRequest(
        @NotNull(message = "标签 id 不能为空")
        Long id,

        @NotBlank(message = "标签名称不能为空")
        @Size(max = 100, message = "最多 100 字符")
        String name
) {
}
