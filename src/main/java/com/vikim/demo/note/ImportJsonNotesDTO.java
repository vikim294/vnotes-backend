package com.vikim.demo.note;

import java.util.List;

public record ImportJsonNotesDTO(
        String version,
        List<ImportDirectoryNodeDTO> directoryNodes,
        List<ImportDetailNodeDTO> detailNodes
) {
}
