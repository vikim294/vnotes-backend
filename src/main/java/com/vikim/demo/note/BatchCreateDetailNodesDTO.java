package com.vikim.demo.note;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record BatchCreateDetailNodesDTO(
        Long parentId,

        @NotNull(message = "节点 UUID 不能为空")
        UUID uuid,
        UUID parentUuid,

        @NotBlank(message = "标签不能为空")
        @Size(max = 8192, message = "最多 8192 字符")
        String label,

        @Digits(integer = 10, fraction = 2)
        @NotNull
        BigDecimal x,

        @Digits(integer = 10, fraction = 2)
        @NotNull
        BigDecimal y
) {

}
