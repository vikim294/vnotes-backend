package com.vikim.demo.note;

public record FindDeletedRootNodesByIdsResponse(
        Long id,
        Long originalParentId
) {
}
