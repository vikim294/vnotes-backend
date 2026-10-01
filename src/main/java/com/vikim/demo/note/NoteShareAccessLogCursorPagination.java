package com.vikim.demo.note;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class NoteShareAccessLogCursorPagination {
    private LocalDateTime createdAt;
    private Long id;
}
