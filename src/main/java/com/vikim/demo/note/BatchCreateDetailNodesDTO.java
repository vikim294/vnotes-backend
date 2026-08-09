package com.vikim.demo.note;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BatchCreateDetailNodesDTO{
        private Long parentId;

        @NotNull(message = "节点 UUID 不能为空")
        private UUID uuid;
        private UUID parentUuid;

        @NotBlank(message = "标签不能为空")
        @Size(max = 8192, message = "最多 8192 字符")
        private String label;

        @Digits(integer = 10, fraction = 2)
        @NotNull
        private BigDecimal x;

        @Digits(integer = 10, fraction = 2)
        @NotNull
        private BigDecimal y;

        @NotNull(message = "节点内容类型不能为空")
        private DetailNodeContentType contentType;

        private DetailNodeMarkType markType;

}
