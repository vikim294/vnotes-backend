package com.vikim.demo.note;

import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteService {
    private final DirectoryNodeMapper directoryNodeMapper;
    private final DetailNodeMapper detailNodeMapper;

    public NoteService(DirectoryNodeMapper directoryNodeMapper, DetailNodeMapper detailNodeMapper) {
        this.directoryNodeMapper = directoryNodeMapper;
        this.detailNodeMapper = detailNodeMapper;
    }

    public Long createDirectoryNode(Long userId, CreateDirectoryNodeRequest request) {
        if (request.parentId() != null && !directoryNodeMapper.hasDirectoryNode(userId, request.parentId(), NodeType.DIRECTORY)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }
        CreateDirectoryNodeMapperParam param = new CreateDirectoryNodeMapperParam(userId, request.label(), request.x(), request.y(), request.nodeType());
        if (request.parentId() != null) {
            param.setParentId(request.parentId());
        }

        directoryNodeMapper.createDirectoryNode(param);
        return param.getId();
    }

    public List<FetchDirectoryNodesResponse> fetchAllDirectoryNodes(Long userId) {
        return directoryNodeMapper.fetchAllDirectoryNodes(userId);
    }

    public void deleteDirectoryNode(Long id, Long userId) {
        int affectedRows = directoryNodeMapper.deleteDirectoryNode(id, userId);
        if (affectedRows == 0) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }
    }

    @Transactional
    public BatchUpdateDirectoryNodesResponse batchUpdateDirectoryNotes(
            Long userId,
            BatchUpdateDirectoryNodesRequest request
    ) {
        Map<UUID, Long> uuidToId = new HashMap<>();
        Set<Long> parentIdsToBeVerified = new HashSet<>();
        Set<Long> deletedIdsToBeVerified = new HashSet<>(request.deletedNodeIds());

        // 遍历 added、updated 找出 parentId 不为 null 的
        Set<Long> parentIdsFromAdded = request.added().stream().filter(node -> node.parentId() != null)
                .map(BatchCreateDirectoryNodesDTO::parentId).collect(Collectors.toSet());

        Set<Long> parentIdsFromUpdated = request.updated().stream().filter(node -> node.getParentId() != null)
                .map(BatchUpdateDirectoryNodesDTO::getParentId).collect(Collectors.toSet());

        parentIdsToBeVerified.addAll(parentIdsFromAdded);
        parentIdsToBeVerified.addAll(parentIdsFromUpdated);

        // 校验 parentId 都是用户自己的
        if(!parentIdsToBeVerified.isEmpty() && parentIdsToBeVerified.size() != directoryNodeMapper.countActiveDirectoryNodesByIds(userId, parentIdsToBeVerified)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_PARENT_VALIDATION_FAILED);
        }

        // 校验 deletedNodeIds 都是用户自己的
        if(!deletedIdsToBeVerified.isEmpty() && deletedIdsToBeVerified.size() != directoryNodeMapper.countActiveDirectoryNodesByIds(userId, deletedIdsToBeVerified)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }

        // TODO 写数据库之前，还要做一些验证：同一批中 uuid 不能重复、parentUuid 必须出现在本批 added 中、parentUuid 不能等于自己的 uuid、新节点之间不能形成 A → B → A 这样的环
        // 如果目录节点是 DETAIL，则还要添加一些验证：DETAIL 节点不能有子目录节点

        // 新增节点、记录 uuidToId
        for (BatchCreateDirectoryNodesDTO addedNode : request.added()) {
            CreateDirectoryNodeMapperParam param = new CreateDirectoryNodeMapperParam(userId, addedNode.label(), addedNode.x(), addedNode.y(), addedNode.nodeType());
            // 旧节点新增的子节点，parentId 是有值的
            if (addedNode.parentId() != null) {
                param.setParentId(addedNode.parentId());
            }

            // 创建目录节点
            int affectedRows = directoryNodeMapper.createDirectoryNode(param);
            if (affectedRows != 1 || param.getId() == null) {
                throw new BusinessException(ErrorCode.DIRECTORY_NODE_CREATED_FAILED);
            }

            // 保存 uuid -> id
            uuidToId.put(addedNode.uuid(), param.getId());
        }

        // 更新 added 节点的 parentId
        for (BatchCreateDirectoryNodesDTO addedNode : request.added()) {
            if (addedNode.parentUuid() != null) {
                Long id = uuidToId.get(addedNode.uuid());
                Long parentId = uuidToId.get(addedNode.parentUuid());

                if (parentId == null) {
                    throw new BusinessException(
                            ErrorCode.INVALID_DIRECTORY_NODE_RELATION
                    );
                }

                // 更新它们的 parentId 为 uuidToId[parentUuid]
                int affectedRows = directoryNodeMapper.updateDirectoryNodeParentId(id, userId, parentId);

                if (affectedRows != 1) {
                    throw new BusinessException(
                            ErrorCode.DIRECTORY_NODE_UPDATED_PARENT_FAILED
                    );
                }
            }
        }

        // 更新 updated 节点
        for (BatchUpdateDirectoryNodesDTO updatedNodes : request.updated()) {
            // 情况1 parentId 传了 -> 传 null 或传值，都更新节点的 parentId
            UpdateDirectoryNodeMapperParam param = new UpdateDirectoryNodeMapperParam(userId, updatedNodes.getParentId(), updatedNodes.getLabel(), updatedNodes.getX(), updatedNodes.getY());

            // updated 节点必有 id
            param.setId(updatedNodes.getId());

            boolean parentIdPresent = updatedNodes.isParentIdPresent();

            // 情况2 传了 parentUuid -> 更新节点的 parentId 为父节点（新增节点）的 id
            if (updatedNodes.getParentUuid() != null) {
                Long parentId = uuidToId.get(updatedNodes.getParentUuid());

                if (parentId == null) {
                    throw new BusinessException(
                            ErrorCode.INVALID_DIRECTORY_NODE_RELATION
                    );
                }

                param.setParentId(parentId);
                parentIdPresent = true;
            }

            // 情况3 前端没传 parentId -> 保持旧 parentId 不变
            param.setParentIdPresent(parentIdPresent);

            int affectedRows = directoryNodeMapper.updateDirectoryNode(param);

            if (affectedRows != 1) {
                throw new BusinessException(
                        ErrorCode.DIRECTORY_NODE_NOT_FOUND
                );
            }
        }

        // 软删除 deletedNodeIds 节点
        if(!deletedIdsToBeVerified.isEmpty()) {
            int affectedRows = directoryNodeMapper.batchDeleteDirectoryNodes(userId, deletedIdsToBeVerified);
            // 删除一个孤立节点 -> 二者相等
            // 删除一个有后代的节点 -> affectedRows > size
            // 所以这里的判断不能用 !=
            if(affectedRows < deletedIdsToBeVerified.size()) {
                throw new BusinessException(ErrorCode.DIRECTORY_NODE_BATCH_DELETE_FAILED);
            }
        }

        return new BatchUpdateDirectoryNodesResponse(uuidToId);
    }

    public List<FetchDetailNodesResponse> fetchDetailNodesByDirectoryNodeId(Long userId, Long directoryNodeId) {
        // 先校验用户是否拥有该 director node
        if (!directoryNodeMapper.hasDirectoryNode(userId, directoryNodeId, NodeType.DETAIL)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }

        return detailNodeMapper.fetchDetailNodesByDirectoryNodeId(directoryNodeId);
    }

    @Transactional
    public BatchUpdateDetailNodesResponse batchUpdateDetailNotes(
            Long userId,
            Long directoryNodeId,
            BatchUpdateDetailNodesRequest request
    ) {
        Map<UUID, Long> uuidToId = new HashMap<>();
        Set<Long> parentIdsToBeVerified = new HashSet<>();
        Set<Long> deletedIdsToBeVerified = new HashSet<>(request.deletedNodeIds());

        // 先校验用户是否拥有该 director node
        if (!directoryNodeMapper.hasDirectoryNode(userId, directoryNodeId, NodeType.DETAIL)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }

        // 遍历 added、updated 找出 parentId 不为 null 的
        Set<Long> parentIdsFromAdded = request.added().stream().filter(node -> node.getParentId() != null)
                .map(BatchCreateDetailNodesDTO::getParentId).collect(Collectors.toSet());

        Set<Long> parentIdsFromUpdated = request.updated().stream().filter(node -> node.getParentId() != null)
                .map(BatchUpdateDetailNodesDTO::getParentId).collect(Collectors.toSet());

        parentIdsToBeVerified.addAll(parentIdsFromAdded);
        parentIdsToBeVerified.addAll(parentIdsFromUpdated);

        // 校验 parentId 都是用户自己的
        if(!parentIdsToBeVerified.isEmpty() && parentIdsToBeVerified.size() != detailNodeMapper.countActiveDetailNodesByIds(directoryNodeId, parentIdsToBeVerified)) {
            throw new BusinessException(ErrorCode.DETAIL_NODE_PARENT_VALIDATION_FAILED);
        }

        // 校验 deletedNodeIds 都是用户自己的
        if(!deletedIdsToBeVerified.isEmpty() && deletedIdsToBeVerified.size() != detailNodeMapper.countActiveDetailNodesByIds(directoryNodeId, deletedIdsToBeVerified)) {
            throw new BusinessException(ErrorCode.DETAIL_NODE_NOT_FOUND);
        }

        // 新增节点、记录 uuidToId
        for (BatchCreateDetailNodesDTO addedNode : request.added()) {
            CreateDetailNodeMapperParam param = new CreateDetailNodeMapperParam(directoryNodeId, addedNode.getLabel(), addedNode.getX(), addedNode.getY(), addedNode.getContentType(), addedNode.getMarkType());
            // 旧节点新增的子节点，parentId 是有值的
            if (addedNode.getParentId() != null) {
                param.setParentId(addedNode.getParentId());
            }

            // 创建详情节点
            int affectedRows = detailNodeMapper.createDetailNode(param);
            if (affectedRows != 1 || param.getId() == null) {
                throw new BusinessException(ErrorCode.DETAIL_NODE_CREATED_FAILED);
            }

            // 保存 uuid -> id
            uuidToId.put(addedNode.getUuid(), param.getId());
        }

        // 更新 added 节点的 parentId
        for (BatchCreateDetailNodesDTO addedNode : request.added()) {
            if (addedNode.getParentUuid() != null) {
                Long id = uuidToId.get(addedNode.getUuid());
                Long parentId = uuidToId.get(addedNode.getParentUuid());

                if (parentId == null) {
                    throw new BusinessException(
                            ErrorCode.INVALID_DETAIL_NODE_RELATION
                    );
                }

                // 更新它们的 parentId 为 uuidToId[parentUuid]
                int affectedRows = detailNodeMapper.updateDetailNodeParentId(id, directoryNodeId, parentId);

                if (affectedRows != 1) {
                    throw new BusinessException(
                            ErrorCode.DETAIL_NODE_UPDATED_PARENT_FAILED
                    );
                }
            }
        }

        // 更新 updated 节点
        for (BatchUpdateDetailNodesDTO updatedNodes : request.updated()) {
            // 情况1 parentId 传了 -> 传 null 或传值，都更新节点的 parentId
            UpdateDetailNodeMapperParam param = new UpdateDetailNodeMapperParam(directoryNodeId, updatedNodes.getParentId(), updatedNodes.getLabel(), updatedNodes.getX(), updatedNodes.getY(), updatedNodes.getMarkType(), updatedNodes.isMarkTypePresent());

            // updated 节点必有 id
            param.setId(updatedNodes.getId());

            boolean parentIdPresent = updatedNodes.isParentIdPresent();

            // 情况2 传了 parentUuid -> 更新节点的 parentId 为父节点（新增节点）的 id
            if (updatedNodes.getParentUuid() != null) {
                Long parentId = uuidToId.get(updatedNodes.getParentUuid());

                if (parentId == null) {
                    throw new BusinessException(
                            ErrorCode.INVALID_DETAIL_NODE_RELATION
                    );
                }

                param.setParentId(parentId);
                parentIdPresent = true;
            }

            // 情况3 前端没传 parentId -> 保持旧 parentId 不变
            param.setParentIdPresent(parentIdPresent);

            int affectedRows = detailNodeMapper.updateDetailNode(param);

            if (affectedRows != 1) {
                throw new BusinessException(
                        ErrorCode.DETAIL_NODE_NOT_FOUND
                );
            }
        }

        // 软删除 deletedNodeIds 节点
        if(!deletedIdsToBeVerified.isEmpty()) {
            int affectedRows = detailNodeMapper.batchDeleteDetailNodes(directoryNodeId, deletedIdsToBeVerified);
            // 删除一个孤立节点 -> 二者相等
            // 删除一个有后代的节点 -> affectedRows > size
            // 所以这里的判断不能用 !=
            if(affectedRows < deletedIdsToBeVerified.size()) {
                throw new BusinessException(ErrorCode.DETAIL_NODE_BATCH_DELETE_FAILED);
            }
        }

        return new BatchUpdateDetailNodesResponse(uuidToId);
    }
}
