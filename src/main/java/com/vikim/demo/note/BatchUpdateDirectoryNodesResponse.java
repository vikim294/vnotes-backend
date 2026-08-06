package com.vikim.demo.note;

import java.util.Map;
import java.util.UUID;

public record BatchUpdateDirectoryNodesResponse(
        Map<UUID, Long> uuidToId
) {
}
