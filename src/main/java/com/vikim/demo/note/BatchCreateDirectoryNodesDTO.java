package com.vikim.demo.note;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record BatchCreateDirectoryNodesDTO(
        Long id,
        Long parentId,

        // TODO
        // 问题1: 嵌套的 DTO 上的校验（比如 @NotBlank），controller 上有 @Valid 时，会校验吗？
        // 问题2：如果会校验，那么 UUID 的非空校验应该用什么？
        @NotNull(message = "节点 UUID 不能为空")
        UUID uuid,
        UUID parentUuid,

        @NotBlank(message = "标签不能为空")
        @Size(max = 255, message = "最多 255 字符")
        String label,

        @Digits(integer = 10, fraction = 2)
        @NotNull
        BigDecimal x,

        @Digits(integer = 10, fraction = 2)
        @NotNull
        BigDecimal y
) {

}
