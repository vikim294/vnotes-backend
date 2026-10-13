package com.vikim.vnotes.note;

public record GetDirectoryNodeByIdResponse(
        Long id,
        String label,
        NodeType nodeType,
        Long version
) {
}
