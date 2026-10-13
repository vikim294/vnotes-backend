package com.vikim.vnotes.note;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BatchUpdateDetailNodesDTO {
    @NotNull(message = "节点 id 不能为空")
    private Long id;
    private Long parentId;
    private UUID parentUuid;

    @Size(max = 8192, message = "最多 8192 字符")
    private String label;

    @Digits(integer = 10, fraction = 2)
    @NotNull
    private BigDecimal x;

    @Digits(integer = 10, fraction = 2)
    @NotNull
    private BigDecimal y;

    @Digits(integer = 10, fraction = 2)
    private BigDecimal imageWidth;

    @Digits(integer = 10, fraction = 2)
    private BigDecimal imageHeight;

    @JsonIgnore // 避免客户端直接传该字段
    private boolean parentIdPresent;

    // 没传 -> 保持旧值
    // 传 null -> 数据库字段改为 null
    // 传 DetailNodeMarkType -> 数据库字段改为 DetailNodeMarkType
    private DetailNodeMarkType markType;

    @JsonIgnore // 避免客户端直接传该字段
    private boolean markTypePresent;

    public void setParentId(Long parentId) {
        this.parentId = parentId;
        this.parentIdPresent = true;
    }

    public void setMarkType(DetailNodeMarkType markType) {
        this.markType = markType;
        this.markTypePresent = true;
    }
}
