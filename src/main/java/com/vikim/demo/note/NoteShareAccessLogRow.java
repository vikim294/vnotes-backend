package com.vikim.demo.note;

import java.time.LocalDateTime;

public record NoteShareAccessLogRow(
        Long id,
        LocalDateTime createdAt
) {
}
