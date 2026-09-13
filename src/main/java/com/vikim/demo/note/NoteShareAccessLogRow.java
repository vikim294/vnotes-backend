package com.vikim.demo.note;

import java.time.LocalDateTime;

public record NoteShareAccessLogRow(
        Long id,
        NoteShareTarget target,
        String targetLabel,
        String visitorId,
        LocalDateTime createdAt
) {
}
