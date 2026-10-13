package com.vikim.vnotes.note;

import java.util.List;

public record ImportJsonNotesDTO(
        String version,
        List<ImportDirectoryNodeDTO> directoryNodes,
        List<ImportDetailNodeDTO> detailNodes,
        List<ImportArticleDTO> articles
) {
}
