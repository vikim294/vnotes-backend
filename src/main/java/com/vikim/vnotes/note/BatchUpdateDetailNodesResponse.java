package com.vikim.vnotes.note;

import java.util.Map;
import java.util.UUID;

public record BatchUpdateDetailNodesResponse(
        Map<UUID, Long> uuidToId,
        Long version
) {
}
