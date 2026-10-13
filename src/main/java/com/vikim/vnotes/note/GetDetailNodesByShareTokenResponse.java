package com.vikim.vnotes.note;

import com.vikim.vnotes.tag.SimpleTagResponse;

import java.util.List;
import java.util.Set;

public record GetDetailNodesByShareTokenResponse(
        String title,
        Set<SimpleTagResponse> tags,
        List<FetchDetailNodesResponse> detailNodes
) {
}
