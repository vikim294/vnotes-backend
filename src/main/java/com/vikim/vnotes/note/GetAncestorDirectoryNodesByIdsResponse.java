package com.vikim.vnotes.note;

public record GetAncestorDirectoryNodesByIdsResponse(
        Long id,
        Long parentId,
        String label
) {
}
