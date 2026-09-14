package com.vikim.demo.note;

import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportService {
    private final ObjectMapper objectMapper;
    private final ImportTransactionService importTransactionService;
    private final NoteTransactionService noteTransactionService;
    private final DirectoryNodeMapper directoryNodeMapper;
    private final ArticleMapper articleMapper;

    @Transactional
    public void importJsonNotes(Long userId, ImportStrategy strategy, MultipartFile file) {
        ImportJsonNotesDTO data = null;

        try {
            data = objectMapper.readValue(
                    file.getInputStream(),
                    ImportJsonNotesDTO.class
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.READ_IMPORTED_FILE_FAILED);
        }

        // TODO 校验
        // 支持的版本
        List<String> validVersions = List.of("1", "2");

        // 校验版本
        if (data.version() == null || !validVersions.contains(data.version())) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_VERSION);
        }

        // 文件格式有问题
        if (data.directoryNodes() == null || data.detailNodes() == null) {
            throw new BusinessException(ErrorCode.READ_IMPORTED_FILE_FAILED);
        }

        // 如果 strategy 是 clear，先删除用户所有笔记
        if (strategy == ImportStrategy.CLEAR) {
            List<DirectoryNodeRow> allDirectoryNodes = directoryNodeMapper.fetchAllDirectoryNodes(userId);
            Set<Long> allDirectoryNodeIds = allDirectoryNodes.stream()
                    .map(DirectoryNodeRow::id)
                    .collect(Collectors.toSet());

            noteTransactionService.softDeleteDirectoryNodesByIds(userId, allDirectoryNodeIds);
        }

        // 构建 map，加速“从 list 中遍历找 parentId 的子节点”
        Map<Long, List<ImportDirectoryNodeDTO>> directoryNodeParentIdMap = new HashMap<>();

        for (ImportDirectoryNodeDTO directoryNodeDTO : data.directoryNodes()) {
            directoryNodeParentIdMap.computeIfAbsent(directoryNodeDTO.sourceParentId(), ignored -> new ArrayList<>())
                    .add(directoryNodeDTO);
        }

        Map<Long, Long> directoryNodeSourceIdToIdMap = new HashMap<>();
        Map<Long, Long> detailNodeSourceIdToIdMap = new HashMap<>();

        // 先递归创建 目录节点
        importTransactionService.createDescendentDirectoryNodesOfId(
                userId,
                null,
                directoryNodeParentIdMap,
                directoryNodeSourceIdToIdMap
        );

        // 再递归创建 详情节点
        List<ImportDirectoryNodeDTO> directoryNodesWithDetail = data.directoryNodes().stream()
                .filter(d -> d.nodeType() == NodeType.DETAIL)
                .toList();

        // 创建每个带详情的 目录节点 的详情
        for (ImportDirectoryNodeDTO directoryNodeWithDetail : directoryNodesWithDetail) {
            importTransactionService.createDescendentDetailNodesOfId(
                    directoryNodeWithDetail.sourceId(),
                    directoryNodeSourceIdToIdMap.get(directoryNodeWithDetail.sourceId()),
                    null,
                    data.detailNodes(),
                    detailNodeSourceIdToIdMap
            );
        }

        // 如果是版本 2 并且有文章，再创建文章

        if("2".equals(data.version())) {
            // 文件格式有问题
            if(data.articles() == null) {
                throw new BusinessException(ErrorCode.READ_IMPORTED_FILE_FAILED);
            }

            for (ImportArticleDTO articleDTO : data.articles()) {
                // 逐个创建 Article 并关联 detailNode
                Long realDetailNodeId = detailNodeSourceIdToIdMap.get(articleDTO.sourceDetailNodeId());

                // 如果当前文章不存在关联的 detail node
                if(realDetailNodeId == null) {
                    throw new BusinessException(ErrorCode.INVALID_ARTICLE_RELATION);
                }

                // 创建文章
                int affectedRows = articleMapper.createArticleForDetailNodeFromImportArticle(new CreateArticleForDetailNodeFromImportArticleMapperParam(
                        realDetailNodeId,
                        articleDTO.content(),
                        articleDTO.createdAt(),
                        articleDTO.updatedAt()
                ));

                if(affectedRows != 1) {
                    throw new BusinessException(ErrorCode.CREATE_ARTICLE_FAILED);
                }
            }
        }
    }
}
