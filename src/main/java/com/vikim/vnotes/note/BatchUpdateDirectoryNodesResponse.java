package com.vikim.vnotes.note;

import java.util.Map;
import java.util.UUID;

public record BatchUpdateDirectoryNodesResponse(
        Map<UUID, Long> uuidToId
) {
}
