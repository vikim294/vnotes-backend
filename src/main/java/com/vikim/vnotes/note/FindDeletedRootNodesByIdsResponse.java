package com.vikim.vnotes.note;

public record FindDeletedRootNodesByIdsResponse(
        Long id,
        Long originalParentId
) {
}
