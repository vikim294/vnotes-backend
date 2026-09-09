package com.vikim.demo.note;

import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NoteTransactionService {
    private final DirectoryNodeMapper directoryNodeMapper;
    private final DetailNodeMapper detailNodeMapper;

    @Transactional
    public void softDeleteDirectoryNodesByIds(Long userId, Set<Long> ids) {
        // --- 先去掉多余的后代节点，得到所有删除根
        Set<Long> deletedRootNodeIds = new HashSet<>(ids);

        // 先构建 id -> parentId map
        Map<Long, Long> idToParentId = new HashMap<>();
        List<DirectoryNodeRow> allDirectoryNodes = directoryNodeMapper.fetchAllDirectoryNodes(userId);

        for (DirectoryNodeRow node : allDirectoryNodes) {
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

        // 为了后面取消分享 已分享的笔记
        Set<Long> directoryNodesWithDetail = new HashSet<>();

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

            // 找出所有带详情的目录节点
            directoryNodesWithDetail.addAll(directoryNodeMapper.findDirectoryNodesWithDetailByRootId(userId, id));
        }

        // 取消分享已分享的笔记
        if(!directoryNodesWithDetail.isEmpty()) {
            directoryNodeMapper.batchCancelNoteShare(directoryNodesWithDetail);
        }
    }

    @Transactional
    public List<FindImageNodesByDirectoryNodesWithDetailResponse> hardDeleteRootNodesFromTrashBin(Long userId, Set<Long> ids){
        // 校验 DeletedRootNodes
        List<FindDeletedRootNodesByIdsResponse> nodes = directoryNodeMapper.findDeletedRootNodesByIds(userId, ids);
        if (nodes.size() != ids.size()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }

        List<FindImageNodesByDirectoryNodesWithDetailResponse> allImageNodes = new ArrayList<>();

        for(Long id: ids) {
            // 找出节点 id 及子树中 所有带详情的目录节点
            List<Long> directoryNodesWithDetailIds = directoryNodeMapper.findDirectoryNodesWithDetailByRootId(userId, id);

            if(!directoryNodesWithDetailIds.isEmpty()) {
                // 找出下面的图片节点
                List<FindImageNodesByDirectoryNodesWithDetailResponse> imageNodes = detailNodeMapper.findImageNodesByDirectoryNodesWithDetail(directoryNodesWithDetailIds);
                allImageNodes.addAll(imageNodes);

                // 删除下面的详情节点
                detailNodeMapper.deleteDetailNodesOfDirectoryRootNodes(directoryNodesWithDetailIds);
            }

            // 删除 root node 目录节点
            if (directoryNodeMapper.deleteRootNodeFromTrashBin(userId, id) != 1) {
                throw new BusinessException(ErrorCode.DIRECTORY_NODE_HARD_DELETE_FAILED);
            }
        }

        return allImageNodes;
    }

    @Transactional
    public CleanUpDeletedRootNodesResponse cleanUpDeletedRootNodes(){
        List<FindImageNodesByDirectoryNodesWithDetailResponse> allImageNodes = new ArrayList<>();

        // 找出所有 deletedRootNotes 和 对应 user
        List<FindAllUsersAndDeletedRootNotesVO> usersAndDeletedRootNotes = directoryNodeMapper.findAllUsersAndDeletedRootNotes();

        for(FindAllUsersAndDeletedRootNotesVO item: usersAndDeletedRootNotes) {
            // 找出节点 id 及子树中 所有带详情的目录节点
            List<Long> directoryNodesWithDetailIds = directoryNodeMapper.findDirectoryNodesWithDetailByRootId(item.userId(), item.deletedRootId());

            if(!directoryNodesWithDetailIds.isEmpty()) {
                // 找出下面的图片节点
                List<FindImageNodesByDirectoryNodesWithDetailResponse> imageNodes = detailNodeMapper.findImageNodesByDirectoryNodesWithDetail(directoryNodesWithDetailIds);
                allImageNodes.addAll(imageNodes);

                // 删除下面的详情节点
                detailNodeMapper.deleteDetailNodesOfDirectoryRootNodes(directoryNodesWithDetailIds);
            }

            // 删除 root node 目录节点
            if (directoryNodeMapper.deleteRootNodeFromTrashBin(item.userId(), item.deletedRootId()) != 1) {
                throw new BusinessException(ErrorCode.DIRECTORY_NODE_HARD_DELETE_FAILED);
            }
        }

        return new CleanUpDeletedRootNodesResponse(allImageNodes, usersAndDeletedRootNotes.size());
    }
}
