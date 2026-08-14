package com.vikim.demo.note;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {
    private final DirectoryNodeMapper directoryNodeMapper;
    private final DetailNodeMapper detailNodeMapper;

    @Transactional(readOnly = true)
    public ExportNotesToJsonResponse exportNotesToJson(Long userId) {
        String version = "1";
        LocalDateTime exportAt = LocalDateTime.now();
        List<ExportDirectoryNodeVO> directoryNodes = directoryNodeMapper.getExportDirectoryNodes(userId);
        List<ExportDetailNodeVO> detailNodes = new ArrayList<>();

        List<Long> directoryNodeIdsWithDetail = directoryNodes.stream()
                .filter(d -> d.nodeType() == NodeType.DETAIL)
                .map(ExportDirectoryNodeVO::sourceId)
                .toList();

        if(!directoryNodeIdsWithDetail.isEmpty()) {
            detailNodes = detailNodeMapper.getExportDetailNodesByDirectoryNodeIds(directoryNodeIdsWithDetail);
        }

        return new ExportNotesToJsonResponse(version, exportAt, directoryNodes, detailNodes);
    }
}
