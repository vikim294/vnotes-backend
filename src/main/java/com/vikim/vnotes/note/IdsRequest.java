package com.vikim.vnotes.note;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record IdsRequest(
        @NotNull(message = "ids 不能为空")
        @NotEmpty(message = "ids 不能为空")
        Set<@NotNull Long> ids
) {
}
