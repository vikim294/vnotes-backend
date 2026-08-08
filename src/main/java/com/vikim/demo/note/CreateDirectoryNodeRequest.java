package com.vikim.demo.note;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateDirectoryNodeRequest(
        Long parentId,

        @NotBlank(message = "标签不能为空")
        @Size(max = 255, message = "最多 255 字符")
        String label,

        @Digits(integer = 10, fraction = 2)
        @NotNull
        BigDecimal x,

        @Digits(integer = 10, fraction = 2)
        @NotNull
        BigDecimal y,

        @NotNull(message = "nodeType 不能为空")
        NodeType nodeType
) {
}
