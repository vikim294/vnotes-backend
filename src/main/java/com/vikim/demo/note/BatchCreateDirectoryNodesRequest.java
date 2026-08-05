package com.vikim.demo.note;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchCreateDirectoryNodesRequest(
        @NotEmpty(message = "新增节点不能为空")
        @Size(max = 100, message = "一次最多新增 100 个节点")
        List<@Valid BatchCreateDirectoryNodesDTO> added
) {
}
