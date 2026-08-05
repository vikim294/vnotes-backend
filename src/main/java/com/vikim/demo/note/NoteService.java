package com.vikim.demo.note;

import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
