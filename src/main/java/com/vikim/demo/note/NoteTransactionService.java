package com.vikim.demo.note;

import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteTransactionService {
    private final DirectoryNodeMapper directoryNodeMapper;

    @Transactional
    public void softDeleteDirectoryNodesByIds(Long userId, Set<Long> ids) {
        // --- 先去掉多余的后代节点，得到所有删除根
        Set<Long> deletedRootNodeIds = new HashSet<>(ids);

        // 先构建 id -> parentId map
        Map<Long, Long> idToParentId = new HashMap<>();
        List<FetchDirectoryNodesResponse> allDirectoryNodes = directoryNodeMapper.fetchAllDirectoryNodes(userId);

        for (FetchDirectoryNodesResponse node : allDirectoryNodes) {
            idToParentId.put(node.id(), node.parentId());
        }

        // 遍历 deletedNodeIds 并处理，得到 deletedRootNodeIds
        for (Long id : ids) {
            // 如果 ids 中包含 id 的父节点/祖先节点，则移除 id
            Long parentId = idToParentId.get(id);
            while (parentId != null) {
                if (ids.contains(parentId)) {
                    deletedRootNodeIds.remove(id);
                    break;
                }

                parentId = idToParentId.get(parentId);
            }
        }

        // --- 对 deletedRootNodeIds 中的每一个根节点
        for (Long id : deletedRootNodeIds) {
            Long originalParentId = idToParentId.get(id);
            if (originalParentId != null) {
                // 更新 originalParentId
                if (directoryNodeMapper.updateDirectoryNodeOriginalParentId(id, userId, originalParentId) != 1) {
                    throw new BusinessException(ErrorCode.DIRECTORY_NODE_UPDATED_ORIGINAL_PARENT_ID_FAILED);
                }

                // 更新 parentId 为 null
                if (directoryNodeMapper.updateDirectoryNodeParentId(id, userId, null) != 1) {
                    throw new BusinessException(ErrorCode.DIRECTORY_NODE_UPDATED_PARENT_FAILED);
                }
            }

            // 更新自身和所有后代的 deletedAt 和 deletedRootId
            if (directoryNodeMapper.batchDeleteDirectoryNodeSubTree(userId, id) < 1) {
                throw new BusinessException(ErrorCode.DIRECTORY_NODE_UPDATED_DELETED_AT_AND_DELETED_ROOT_ID_FAILED);
            }
        }
    }
}
