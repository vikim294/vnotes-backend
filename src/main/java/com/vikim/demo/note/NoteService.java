package com.vikim.demo.note;

import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NoteService {
    private final DirectoryNodeMapper directoryNodeMapper;

    public NoteService(DirectoryNodeMapper directoryNodeMapper) {
        this.directoryNodeMapper = directoryNodeMapper;
    }

    public Long createDirectoryNode(Long userId, CreateDirectoryNodeRequest request) {
        if (request.parentId() != null && !directoryNodeMapper.hasDirectoryNode(userId, request.parentId())) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }
        CreateDirectoryNodeMapperParam param = new CreateDirectoryNodeMapperParam(userId, request.label(), request.x(), request.y());
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
        int affectedRows =  directoryNodeMapper.deleteDirectoryNode(id, userId);
        if(affectedRows == 0) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }
    }

    // TODO: 要不要 @Transactional ？
    @Transactional
    public BatchCreateDirectoryNodesResponse batchCreateDirectoryNotes(
            Long userId,
            BatchCreateDirectoryNodesRequest request
    ) {
        Map<UUID, Long> uuidToId = new HashMap<>();

        // TODO 写数据库之前，还要做一些验证：同一批中 uuid 不能重复、parentUuid 必须出现在本批 added 中、parentUuid 不能等于自己的 uuid、新节点之间不能形成 A → B → A 这样的环

        for(BatchCreateDirectoryNodesDTO addedNode: request.added()) {
            CreateDirectoryNodeMapperParam param = new CreateDirectoryNodeMapperParam(userId, addedNode.label(), addedNode.x(), addedNode.y());
            // 新增 旧节点的子节点，parentId 是有值的
            if(addedNode.parentId() != null) {
                param.setParentId(addedNode.parentId());
            }

            // 创建目录节点
            int affectedRows = directoryNodeMapper.createDirectoryNode(param);
            if(affectedRows != 1 || param.getId() == null) {
                throw new BusinessException(ErrorCode.DIRECTORY_NODE_CREATED_FAILED);
            }

            // 保存 uuid -> id
            uuidToId.put(addedNode.uuid(), param.getId());
        }

        // TODO 不知道执行到这里时，上面的数据库创建目录节点都 ok 了吗
        for(BatchCreateDirectoryNodesDTO addedNode: request.added()) {
            if(addedNode.parentUuid() != null) {
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

        return new BatchCreateDirectoryNodesResponse(uuidToId);
    }
}
