package com.vikim.demo.note;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FetchDirectoryNodesResponse(
        Long id,
        String label,
        BigDecimal x,
        BigDecimal y,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
