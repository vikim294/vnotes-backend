package com.vikim.demo.note;

import java.util.List;

public record FetchDetailNodesByDirectoryNodeIdResponse(
        Long version,
        List<FetchDetailNodesResponse> detailNodes
) {
}
