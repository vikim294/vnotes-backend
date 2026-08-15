package com.vikim.demo.note;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ImportTransactionService {
    private final DirectoryNodeMapper directoryNodeMapper;
    private final DetailNodeMapper detailNodeMapper;

    @Transactional
    // 创建目录节点 id 的后代节点
    public void createDescendentDirectoryNodesOfId(
            Long userId,
            Long parentId,
            Map<Long, List<ImportDirectoryNodeDTO>> directoryNodeParentIdMap,
            Map<Long, Long> directoryNodeSourceIdToIdMap
    ) {
        for (ImportDirectoryNodeDTO directoryNode : directoryNodeParentIdMap.getOrDefault(parentId, List.of())) {
            // 创建 directoryNode
            CreateDirectoryNodeFromImportMapperParam param = new CreateDirectoryNodeFromImportMapperParam(
                    userId,
                    parentId == null ? null : directoryNodeSourceIdToIdMap.get(parentId),
                    directoryNode.label(),
                    directoryNode.x(),
                    directoryNode.y(),
                    directoryNode.nodeType(),
                    directoryNode.createdAt(),
                    directoryNode.updatedAt()
            );

            directoryNodeMapper.createDirectoryNodeFromImport(param);

            // 得到数据库生成的 id
            Long newId = param.getId();

            // 保存 旧 id -> 新 id 映射
            directoryNodeSourceIdToIdMap.put(directoryNode.sourceId(), newId);

            // 递归创建 directoryNode 的后代节点
            createDescendentDirectoryNodesOfId(userId, directoryNode.sourceId(), directoryNodeParentIdMap, directoryNodeSourceIdToIdMap);
        }
    }

    // 创建详情节点 id 的后代节点
    @Transactional
    public void createDescendentDetailNodesOfId(
            Long sourceDirectoryNodeId,
            Long directoryNodeId,
            Long parentId,
            List<ImportDetailNodeDTO> flatDetailNodesList,
            Map<Long, Long> detailNodeSourceIdToIdMap
    ) {
        // 遍历 list 找出 item.parentId 为 parentId 的节点
        for (ImportDetailNodeDTO detailNode : flatDetailNodesList) {
            if (Objects.equals(detailNode.sourceDirectoryNodeId(), sourceDirectoryNodeId) && Objects.equals(detailNode.sourceParentId(), parentId)) {
                // 创建 detailNode
                CreateDetailNodeFromImportMapperParam param = new CreateDetailNodeFromImportMapperParam(
                        directoryNodeId,
                        parentId == null ? null : detailNodeSourceIdToIdMap.get(parentId),
                        detailNode.label(),
                        detailNode.x(),
                        detailNode.y(),
                        detailNode.imageWidth(),
                        detailNode.imageHeight(),
                        detailNode.contentType(),
                        detailNode.markType(),
                        detailNode.createdAt(),
                        detailNode.updatedAt()
                );

                detailNodeMapper.createDetailNodeFromImport(param);

                // 得到数据库生成的 id
                Long newId = param.getId();

                // 保存 旧 id -> 新 id 映射
                detailNodeSourceIdToIdMap.put(detailNode.sourceId(), newId);

                // 递归创建 directoryNode 的后代节点
                createDescendentDetailNodesOfId(sourceDirectoryNodeId, directoryNodeId, detailNode.sourceId(), flatDetailNodesList, detailNodeSourceIdToIdMap);
            }
        }
    }
}
