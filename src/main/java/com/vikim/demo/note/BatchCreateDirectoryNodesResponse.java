package com.vikim.demo.note;

import java.util.Map;
import java.util.UUID;

public record BatchCreateDirectoryNodesResponse(
        Map<UUID, Long> uuidToId
) {
}
