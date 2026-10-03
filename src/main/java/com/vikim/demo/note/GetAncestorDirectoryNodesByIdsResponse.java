package com.vikim.demo.note;

public record GetAncestorDirectoryNodesByIdsResponse(
        Long id,
        Long parentId,
        String label
) {
}
