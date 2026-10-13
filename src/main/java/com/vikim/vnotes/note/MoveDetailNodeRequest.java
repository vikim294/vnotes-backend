package com.vikim.vnotes.note;

import jakarta.validation.constraints.NotNull;

public record MoveDetailNodeRequest(
        @NotNull(message = "sourceVersion 不能为空")
        Long sourceVersion,

        @NotNull(message = "详情节点不能为空")
        Long detailNodeId,

        @NotNull(message = "目的地目录节点不能为空")
        Long destDirectoryNodeId
) {
}
