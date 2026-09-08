package com.vikim.demo.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
        @NotBlank(message = "标签名称不能为空")
        @Size(max = 100, message = "最多 100 字符")
        String name
) {
}
