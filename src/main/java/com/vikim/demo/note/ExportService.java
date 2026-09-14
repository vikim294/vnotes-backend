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
    private final ArticleMapper articleMapper;

    @Transactional(readOnly = true)
    public ExportNotesToJsonResponse exportNotesToJson(Long userId) {
        // 1 只导出目录和详情节点，不包含图片
        // 2 文章也导出
        String version = "2";
        LocalDateTime exportAt = LocalDateTime.now();
        List<ExportDirectoryNodeVO> directoryNodes = directoryNodeMapper.getExportDirectoryNodes(userId);
        List<ExportDetailNodeVO> detailNodes = new ArrayList<>();
        List<ExportArticleVO> articles = new ArrayList<>();

        List<Long> directoryNodeIdsWithDetail = directoryNodes.stream()
                .filter(d -> d.nodeType() == NodeType.DETAIL)
                .map(ExportDirectoryNodeVO::sourceId)
                .toList();

        if(!directoryNodeIdsWithDetail.isEmpty()) {
            detailNodes = detailNodeMapper.getExportDetailNodesByDirectoryNodeIds(directoryNodeIdsWithDetail);
        }

        if(!detailNodes.isEmpty()) {
            articles = articleMapper.getExportArticlesByDetailNodeIds(detailNodes.stream()
                    .map(ExportDetailNodeVO::sourceId)
                    .toList());
        }

        return new ExportNotesToJsonResponse(version, exportAt, directoryNodes, detailNodes, articles);
    }
}
