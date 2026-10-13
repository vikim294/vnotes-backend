package com.vikim.vnotes.note;

import java.util.List;

public record FetchNoteShareAccessLogsByNoteShareIdResponse(
        List<NoteShareAccessLogResponse> list,
        boolean hasMore,
        String nextCursor
) {
}
