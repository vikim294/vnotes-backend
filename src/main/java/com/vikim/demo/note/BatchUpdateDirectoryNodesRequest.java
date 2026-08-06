package com.vikim.demo.note;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchUpdateDirectoryNodesRequest(
        @NotNull(message = "added 不能为空")
        @Size(max = 1000, message = "一次最多新增 1000 个节点")
        List<@Valid BatchCreateDirectoryNodesDTO> added,

        @NotNull(message = "updated 不能为空")
        @Size(max = 1000, message = "一次最多更新 1000 个节点")
        List<@Valid BatchUpdateDirectoryNodesDTO> updated,

        @NotNull(message = "deletedNodeIds 不能为空")
        @Size(max = 1000, message = "一次最多删除 1000 个节点")
        List<@NotNull Long> deletedNodeIds
) {
}
