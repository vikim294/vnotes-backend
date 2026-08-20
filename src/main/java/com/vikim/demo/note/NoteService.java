package com.vikim.demo.note;

import com.vikim.demo.common.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final DirectoryNodeMapper directoryNodeMapper;
    private final DetailNodeMapper detailNodeMapper;
    private final FileService fileService;
    private final FileUploadProperties fileUploadProperties;
    private final DetailNodeTransactionService detailNodeTransactionService;
    private final NoteTransactionService noteTransactionService;

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

    public PageResponse<FetchDirectoryNodesResponse> searchDirectoryNodes(Long userId, String keyword, int pageSize, int page, NodeType nodeType, LocalDateTime startTime, LocalDateTime endTime) {
        SearchDirectoryNodeMapperParam param = new SearchDirectoryNodeMapperParam(userId, keyword, pageSize, (page - 1) * pageSize, nodeType, startTime, endTime);

        List<FetchDirectoryNodesResponse> responses = directoryNodeMapper.searchDirectoryNodes(param);
        int total = directoryNodeMapper.countDirectoryNodes(param);

        return new PageResponse<>(responses, page, pageSize, total);
    }

    public PageResponse<SearchDirectoryNodesInTrashBinResponse> searchDirectoryNodesInTrashBin(Long userId, String keyword, int pageSize, int page, NodeType nodeType, LocalDateTime startTime, LocalDateTime endTime) {
        SearchDirectoryNodeInTrashBinMapperParam param = new SearchDirectoryNodeInTrashBinMapperParam(userId, keyword, pageSize, (page - 1) * pageSize, nodeType, startTime, endTime);

        List<SearchDirectoryNodesInTrashBinResponse> responses = directoryNodeMapper.searchDirectoryNodesInTrashBin(param);
        int total = directoryNodeMapper.countDirectoryNodesInTrashBin(param);

        return new PageResponse<>(responses, page, pageSize, total);
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
        if (!parentIdsToBeVerified.isEmpty() && parentIdsToBeVerified.size() != directoryNodeMapper.countActiveDirectoryNodesByIds(userId, parentIdsToBeVerified)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_PARENT_VALIDATION_FAILED);
        }

        // 校验 deletedNodeIds 都是用户自己的
        if (!deletedIdsToBeVerified.isEmpty() && deletedIdsToBeVerified.size() != directoryNodeMapper.countActiveDirectoryNodesByIds(userId, deletedIdsToBeVerified)) {
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

        // 处理 deletedNodeIds
        noteTransactionService.softDeleteDirectoryNodesByIds(userId, deletedIdsToBeVerified);

        return new BatchUpdateDirectoryNodesResponse(uuidToId);
    }

    public List<FetchDetailNodesResponse> fetchDetailNodesByDirectoryNodeId(Long userId, Long directoryNodeId) {
        // 先校验用户是否拥有该 director node
        if (!directoryNodeMapper.hasDirectoryNode(userId, directoryNodeId, NodeType.DETAIL)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }

        return detailNodeMapper.fetchDetailNodesByDirectoryNodeId(directoryNodeId);
    }

    public BatchUpdateDetailNodesResponse batchUpdateDetailNotes(
            Long userId,
            Long directoryNodeId,
            BatchUpdateDetailNodesRequest request,
            Map<String, MultipartFile> files
    ) {
        // --- 先校验 节点是否是用户的
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
        if (!parentIdsToBeVerified.isEmpty() && parentIdsToBeVerified.size() != detailNodeMapper.countActiveDetailNodesByIds(directoryNodeId, parentIdsToBeVerified)) {
            throw new BusinessException(ErrorCode.DETAIL_NODE_PARENT_VALIDATION_FAILED);
        }

        // 校验 deletedNodeIds 都是用户自己的
        if (!deletedIdsToBeVerified.isEmpty() && deletedIdsToBeVerified.size() != detailNodeMapper.countActiveDetailNodesByIds(directoryNodeId, deletedIdsToBeVerified)) {
            throw new BusinessException(ErrorCode.DETAIL_NODE_NOT_FOUND);
        }

        for (BatchCreateDetailNodesDTO addedNode : request.added()) {
            // 校验：added 的文本节点对应的 label 是否为空
            if (addedNode.getContentType() == DetailNodeContentType.TEXT) {
                if (addedNode.getLabel() == null || addedNode.getLabel().trim().isBlank()) {
                    throw new BusinessException(ErrorCode.TEXT_NODE_LABEL_IS_NULL);
                }
            }
        }

        for (BatchUpdateDetailNodesDTO updatedNode : request.updated()) {
            // 校验：updated 的文本节点对应的 label 是否为空
            FetchDetailNodesResponse existingNode =
                    detailNodeMapper
                            .getDetailNodeById(
                                    updatedNode.getId(),
                                    directoryNodeId
                            )
                            .orElseThrow(() ->
                                    new BusinessException(
                                            ErrorCode.DETAIL_NODE_NOT_FOUND
                                    )
                            );
            if (existingNode.contentType() == DetailNodeContentType.TEXT) {
                if (updatedNode.getLabel() == null || updatedNode.getLabel().trim().isBlank()) {
                    throw new BusinessException(ErrorCode.TEXT_NODE_LABEL_IS_NULL);
                }
            }
        }

        List<BatchCreateDetailNodesDTO> imageNodes = request.added().stream().filter(node -> node.getContentType() == DetailNodeContentType.IMAGE).toList();

        // 校验：added 的图片节点和 files 是否一一对应
        Set<String> expectedFileNames = imageNodes.stream().map(node -> "file." + node.getUuid()).collect(Collectors.toSet());
        if (!files.keySet().equals(expectedFileNames)) {
            throw new BusinessException(
                    ErrorCode.IMAGE_NODE_FILES_NOT_MATCH
            );
        }

        Map<UUID, UploadImageOfNodeVO> succeededImageNodes = new HashMap<>();
        List<Path> filesToBeDeletedWhenError = new ArrayList<>();

        List<ImageDetailNodeResponse> imageNodesInvolved = new ArrayList<>();
        if (!deletedIdsToBeVerified.isEmpty()) {
            imageNodesInvolved = detailNodeMapper.getImageNodesFromIds(directoryNodeId, deletedIdsToBeVerified);
        }

        try {
            // --- 先保存图片节点的图片文件
            for (BatchCreateDetailNodesDTO imageNode : imageNodes) {
                MultipartFile file = files.get("file." + imageNode.getUuid());

                if (file.isEmpty()) {
                    throw new BusinessException(ErrorCode.FILE_IS_NOT_PRESENT);
                }

                UploadImageOfNodeVO vo = new UploadImageOfNodeVO();
                vo.setUuid(imageNode.getUuid());

                // 生成随机文件名
                String filename = UUID.randomUUID()
                        + fileService.getImageExtensionByFileMimeType(file);

                // 文件保存目录
                Path dir = Paths.get(fileUploadProperties.uploadDir(), "imageNodes");

                // 文件保存 url
                Path target =
                        dir.resolve(filename);

                try {
                    Files.createDirectories(dir);
                } catch (IOException e) {
                    throw new BusinessException(ErrorCode.DIR_CREATED_FAILED);
                }

                try {
                    // 写入文件到本地
                    file.transferTo(target);
                } catch (IOException e) {
                    fileService.deleteQuietly(target);

                    filesToBeDeletedWhenError.add(target);
                    throw new BusinessException(ErrorCode.FILE_WRITING_FAILED);
                }

                // 文件访问 url
                String fileUrl = "/uploads/imageNodes/" + filename;

                vo.setUrl(fileUrl);
                filesToBeDeletedWhenError.add(target);
                succeededImageNodes.put(vo.getUuid(), vo);
            }

            // 再更新数据库
            BatchUpdateDetailNodesResponse response = detailNodeTransactionService.batchUpdateDetailNotesToDB(directoryNodeId, request, succeededImageNodes, deletedIdsToBeVerified);

            // 删除 deleted 节点 相关图片节点对应的文件
            deleteFilesOfImageNodes(imageNodesInvolved.stream().map(ImageDetailNodeResponse::imageUrl).toList());

            return response;
        } catch (RuntimeException e) {
            // 清理已保存的文件
            fileService.deleteQuietly(filesToBeDeletedWhenError);
            throw e;
        }
    }

    @Transactional
    public void restoreDirectoryNodesFromTrashBinByIds(Long userId, IdsRequest request) {
        // 找出所有 ids 节点
        List<FindDeletedRootNodesByIdsResponse> nodes = directoryNodeMapper.findDeletedRootNodesByIds(userId, request.ids());

        if (nodes.size() != request.ids().size()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }

        // 必须先把所有待恢复节点及子节点的 deletedAt 更新为 null
        for (FindDeletedRootNodesByIdsResponse node : nodes) {
            // 更新 id 和其后代节点的 deletedAt 和 deletedRootId 为 null
            if (directoryNodeMapper.batchRestoreDirectoryNodeSubTree(userId, node.id()) < 1) {
                throw new BusinessException(ErrorCode.DIRECTORY_NODE_BATCH_RESTORE_FAILED);
            }
        }

        // 然后再处理 originalParentId -> parentId 的更新
        for (FindDeletedRootNodesByIdsResponse node : nodes) {
            // 根据 original parent id 存在性
            if (node.originalParentId() != null) {
                Long parentId = directoryNodeMapper.hasDirectoryNode(userId, node.originalParentId(), NodeType.DIRECTORY) ? node.originalParentId() : null;
                // 更新节点 id 的 parentId、original parent id 更新为 null
                if (directoryNodeMapper.restoreDirectoryNodeParent(node.id(), userId, parentId) != 1) {
                    throw new BusinessException(ErrorCode.DIRECTORY_NODE_RESTORE_PARENT_FAILED);
                }
            }
        }
    }

    public void hardDeleteFromTrashBin(Long userId, IdsRequest request){
        List<FindImageNodesByDirectoryNodesWithDetailResponse> imageNodesInvolved = noteTransactionService.hardDeleteRootNodesFromTrashBin(userId, request.ids());

        deleteFilesOfImageNodes(imageNodesInvolved.stream().map(FindImageNodesByDirectoryNodesWithDetailResponse::imageUrl).toList());
    }

    // 删除图片节点对应的文件
    private void deleteFilesOfImageNodes(List<String> imageUrls) {
        List<Path> paths = imageUrls.stream().map(url -> {
            String fileUrl = url.substring(url.indexOf("imageNodes/"));
            Path dir = Paths.get(fileUploadProperties.uploadDir());
            return dir.resolve(fileUrl);
        }).toList();
        fileService.deleteQuietly(paths);
    }

    public CleanUpDeletedRootNodesResponse cleanUpDeletedRootNodes(){
        CleanUpDeletedRootNodesResponse response = noteTransactionService.cleanUpDeletedRootNodes();

        deleteFilesOfImageNodes(response.imageNodes().stream().map(FindImageNodesByDirectoryNodesWithDetailResponse::imageUrl).toList());

        return response;
    }
}
