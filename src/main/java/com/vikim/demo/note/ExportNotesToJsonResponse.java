package com.vikim.demo.note;

import java.time.LocalDateTime;
import java.util.List;

public record ExportNotesToJsonResponse(
        String version,
        LocalDateTime exportAt,
        List<ExportDirectoryNodeVO> directoryNodes,
        List<ExportDetailNodeVO> detailNodes,
        List<ExportArticleVO> articles
) {
}
