package com.vikim.demo.note;

import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DetailNodeTransactionService {

    private final DetailNodeMapper detailNodeMapper;
    private final ArticleMapper articleMapper;

    // 插入、更新 数据库操作
    @Transactional
    public BatchUpdateDetailNodesTransactionResult batchUpdateDetailNotesToDB(
            Long directoryNodeId,
            BatchUpdateDetailNodesRequest request,
            Map<UUID, UploadImageOfNodeVO> succeededImageNodes,
            Set<Long> deletedIdsToBeVerified
    ) {
        Map<UUID, Long> uuidToId = new HashMap<>();

        // --- 新增节点、记录 uuidToId
        for (BatchCreateDetailNodesDTO addedNode : request.added()) {
            String imageUrl = null;
            if(addedNode.getContentType() == DetailNodeContentType.IMAGE) {
                imageUrl = succeededImageNodes.get(addedNode.getUuid()).getUrl();
            }
            CreateDetailNodeMapperParam param = new CreateDetailNodeMapperParam(directoryNodeId, addedNode.getLabel(), imageUrl, addedNode.getX(), addedNode.getY(), addedNode.getImageWidth(), addedNode.getImageHeight(), addedNode.getContentType(), addedNode.getMarkType(), addedNode.getRefDirectoryNodeId(), addedNode.getRefDetailNodeId());
            // 旧节点新增的子节点，parentId 是有值的
            if (addedNode.getParentId() != null) {
                param.setParentId(addedNode.getParentId());
            }

            // todo 后续改成批量校验（这种校验 怎么改？）
            validateCreateParam(param);

            // 创建详情节点
            int affectedRows = detailNodeMapper.createDetailNode(param);
            if (affectedRows != 1 || param.getId() == null) {
                throw new BusinessException(ErrorCode.DETAIL_NODE_CREATED_FAILED);
            }

            if(addedNode.isArticle()) {
                // 创建 article
                if(articleMapper.createArticleForDetailNode(param.getId()) != 1) {
                    throw new BusinessException(ErrorCode.CREATE_ARTICLE_FAILED);
                }
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

        // --- 更新 updated 节点
        for (BatchUpdateDetailNodesDTO updatedNodes : request.updated()) {
            // 情况1 parentId 传了 -> 传 null 或传值，都更新节点的 parentId
            UpdateDetailNodeMapperParam param = new UpdateDetailNodeMapperParam(directoryNodeId, updatedNodes.getParentId(), updatedNodes.getLabel(), updatedNodes.getX(), updatedNodes.getY(), updatedNodes.getImageWidth(), updatedNodes.getImageHeight(), updatedNodes.getMarkType(), updatedNodes.isMarkTypePresent());

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

        List<String> deletedImageObjectKeys = new ArrayList<>();

        if(!deletedIdsToBeVerified.isEmpty()) {
            // 查最新的、涉及的图片节点
            deletedImageObjectKeys = detailNodeMapper.getImageNodesFromIds(directoryNodeId, deletedIdsToBeVerified).stream()
                    .map(ImageDetailNodeResponse::imageUrl)
                    .toList();

            // 硬删除 deletedNodeIds 节点
            detailNodeMapper.batchDeleteDetailNodes(directoryNodeId, deletedIdsToBeVerified);
        }

        return new BatchUpdateDetailNodesTransactionResult(new BatchUpdateDetailNodesResponse(uuidToId), deletedImageObjectKeys);
    }

    private void validateCreateParam(CreateDetailNodeMapperParam param){
        switch (param.getContentType()) {
            case REF_NOTE -> {
                boolean res = detailNodeMapper.validateRefNodeForCreate(param.getContentType(), param.getRefDirectoryNodeId(), param.getDirectoryNodeId());
                if(!res) {
                    throw new BusinessException(ErrorCode.REF_NODE_VALIDATED_FAILED);
                }
            }
            case REF_DETAIL, REF_ARTICLE -> {
                boolean res = detailNodeMapper.validateRefNodeForCreate(param.getContentType(), param.getRefDetailNodeId(), param.getDirectoryNodeId());
                if(!res) {
                    throw new BusinessException(ErrorCode.REF_NODE_VALIDATED_FAILED);
                }
            }
        }
    }
}
