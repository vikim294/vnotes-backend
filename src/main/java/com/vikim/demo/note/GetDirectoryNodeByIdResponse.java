package com.vikim.demo.note;

public record GetDirectoryNodeByIdResponse(
        Long id,
        String label,
        NodeType nodeType
) {
}
