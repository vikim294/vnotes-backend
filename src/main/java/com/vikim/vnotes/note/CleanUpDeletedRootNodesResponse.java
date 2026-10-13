package com.vikim.vnotes.note;

import java.util.List;

public record CleanUpDeletedRootNodesResponse(
        List<FindImageNodesByDirectoryNodesWithDetailResponse> imageNodes,
        int deletedRootNum
) {
}
