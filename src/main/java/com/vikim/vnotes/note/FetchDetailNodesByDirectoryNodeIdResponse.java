package com.vikim.vnotes.note;

import java.util.List;

public record FetchDetailNodesByDirectoryNodeIdResponse(
        Long version,
        List<FetchDetailNodesResponse> detailNodes
) {
}
