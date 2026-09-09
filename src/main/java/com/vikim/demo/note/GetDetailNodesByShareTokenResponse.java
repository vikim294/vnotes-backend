package com.vikim.demo.note;

import com.vikim.demo.tag.SimpleTagResponse;

import java.util.List;
import java.util.Set;

public record GetDetailNodesByShareTokenResponse(
        String title,
        Set<SimpleTagResponse> tags,
        List<FetchDetailNodesResponse> detailNodes
) {
}
