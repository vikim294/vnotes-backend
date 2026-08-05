package com.vikim.demo.note;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {
    private final DirectoryNodeMapper directoryNodeMapper;

    public NoteService(DirectoryNodeMapper directoryNodeMapper){
        this.directoryNodeMapper = directoryNodeMapper;
    }

    public void createDirectoryNode(Long userId, CreateDirectoryNodeRequest request) {
        directoryNodeMapper.createDirectoryNode(userId, request.label(), request.x(), request.y());
    }

    public List<FetchDirectoryNodesResponse> fetchAllDirectoryNodes(Long userId) {
        return directoryNodeMapper.fetchAllDirectoryNodes(userId);
    }
}
