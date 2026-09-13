package com.vikim.demo.note;

import com.vikim.demo.common.*;
import com.vikim.demo.objectStorage.ObjectStorageService;
import com.vikim.demo.tag.DirectoryNodeTagMapper;
import com.vikim.demo.tag.SimpleTagResponse;
import com.vikim.demo.tag.TagMapper;
import com.vikim.demo.tag.TagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    private final ObjectStorageService objectStorageService;
    private final DirectoryNodeTagMapper directoryNodeTagMapper;
    private final TagMapper tagMapper;
    private final NoteShareAccessLogAsyncService noteShareAccessLogAsyncService;
    private final ArticleMapper articleMapper;

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
        return directoryNodeMapper.fetchAllDirectoryNodes(userId).stream()
                .map(FetchDirectoryNodesResponse::from).toList();
    }

    public PageResponse<FetchDirectoryNodesWithTagsResponse> searchDirectoryNodes(Long userId, String keyword, int pageSize, int page, NodeType nodeType, Set<Long> tagIds, LocalDateTime startTime, LocalDateTime endTime) {
        // 校验 tags 都是用户的
        if (tagIds != null && !tagIds.isEmpty() && tagIds.size() != tagMapper.checkUserHasTags(userId, tagIds)) {
            throw new BusinessException(ErrorCode.INVALID_TAGS);
        }

        SearchDirectoryNodeMapperParam param = new SearchDirectoryNodeMapperParam(userId, keyword, pageSize, (page - 1) * pageSize, nodeType, tagIds, startTime, endTime);

        // 先找符合条件的 一页目录节点 id
        List<Long> ids = directoryNodeMapper.searchDirectoryNodes(param);

        // 再根据 id 找数据
        List<DirectoryNodeWithTagRow> rows = new ArrayList<>();

        // ids 非空才找
        if (!ids.isEmpty()) {
            rows = directoryNodeMapper.getDirectoryNodesByIds(ids);
        }

        // 组装响应
        Map<Long, FetchDirectoryNodesWithTagsResponse> responseMap = new HashMap<>();

        for (DirectoryNodeWithTagRow row : rows) {
            FetchDirectoryNodesWithTagsResponse item = responseMap.get(row.id());

            SimpleTagResponse simpleTagResponse = new SimpleTagResponse(
                    row.tagId(),
                    row.tagName()
            );
            if (item == null) {
                // first one
                Set<SimpleTagResponse> tags = new HashSet<>();
                if (simpleTagResponse.id() != null) {
                    tags.add(simpleTagResponse);
                }

                responseMap.put(row.id(), new FetchDirectoryNodesWithTagsResponse(
                        row.id(),
                        row.parentId(),
                        row.label(),
                        row.x(),
                        row.y(),
                        row.nodeType(),
                        row.shared(),
                        tags,
                        row.createdAt().toInstant(ZoneOffset.UTC),
                        row.updatedAt().toInstant(ZoneOffset.UTC)
                ));
            } else {
                if (simpleTagResponse.id() != null) {
                    item.getTags().add(simpleTagResponse);
                }
            }
        }

        int total = directoryNodeMapper.countDirectoryNodes(param);

        // 按分页查询结果 ids 的顺序返回响应
        return new PageResponse<>(ids.stream().map(responseMap::get).toList(), page, pageSize, total);
    }

    public PageResponse<SearchDirectoryNodesInTrashBinResponse> searchDirectoryNodesInTrashBin(Long userId, String keyword, int pageSize, int page, NodeType nodeType, LocalDateTime startTime, LocalDateTime endTime) {
        SearchDirectoryNodeInTrashBinMapperParam param = new SearchDirectoryNodeInTrashBinMapperParam(userId, keyword, pageSize, (page - 1) * pageSize, nodeType, startTime, endTime);

        List<SearchDirectoryNodesInTrashBinResponse> responses = directoryNodeMapper.searchDirectoryNodesInTrashBin(param).stream()
                .map(SearchDirectoryNodesInTrashBinResponse::from).toList();
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

            Long id = param.getId();

            // 保存 uuid -> id
            uuidToId.put(addedNode.uuid(), id);

            // 如果是带详情的目录节点 + 传了 tagIds
            if (addedNode.nodeType() == NodeType.DETAIL && addedNode.tagIds() != null && !addedNode.tagIds().isEmpty()) {
                // 去重
                Set<Long> tagIds = new HashSet<>(addedNode.tagIds());

                // 校验 tags 都是用户自己的
                if (tagMapper.checkUserHasTags(userId, tagIds) != tagIds.size()) {
                    throw new BusinessException(ErrorCode.INVALID_TAGS);
                }

                // 关联 目录节点 和 tags
                directoryNodeTagMapper.batchCreateDirectoryNodeTags(id, tagIds);
            }
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

            // 如果是带详情的目录节点
            int count = directoryNodeMapper.isWithDetail(updatedNodes.getId());
            // 传了 tagIds
            if (count == 1 && updatedNodes.getTagIds() != null) {
                // 去重
                Set<Long> tagIds = new HashSet<>(updatedNodes.getTagIds());

                // 校验 tags 都是用户自己的
                if (!tagIds.isEmpty() && tagMapper.checkUserHasTags(userId, tagIds) != tagIds.size()) {
                    throw new BusinessException(ErrorCode.INVALID_TAGS);
                }

                // 无论传入的 tagIds 是不是空数组，删除已有的关联记录
                directoryNodeTagMapper.batchDeleteDirectoryNodeTags(updatedNodes.getId());

                // tagIds 不是空数组
                if (!tagIds.isEmpty()) {
                    // 关联 目录节点 和 tags
                    directoryNodeTagMapper.batchCreateDirectoryNodeTags(updatedNodes.getId(), tagIds);
                }
            }
        }

        // 处理 deletedNodeIds
        noteTransactionService.softDeleteDirectoryNodesByIds(userId, deletedIdsToBeVerified);

        return new BatchUpdateDirectoryNodesResponse(uuidToId);
    }

    public List<FetchDetailNodesResponse> oldFetchDetailNodesByDirectoryNodeId(Long userId, Long directoryNodeId) {
        // 先校验用户是否拥有该 director node
        if (!directoryNodeMapper.hasDirectoryNode(userId, directoryNodeId, NodeType.DETAIL)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }

        return detailNodeMapper.fetchDetailNodesByDirectoryNodeId(directoryNodeId).stream()
                .map(FetchDetailNodesResponse::from).toList();
    }

    public List<FetchDetailNodesResponse> fetchDetailNodesByDirectoryNodeId(Long userId, Long directoryNodeId) {
        // 先校验用户是否拥有该 director node
        if (!directoryNodeMapper.hasDirectoryNode(userId, directoryNodeId, NodeType.DETAIL)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }

        return detailNodeMapper.fetchDetailNodesByDirectoryNodeId(directoryNodeId).stream()
                .map(row -> {
                    return new FetchDetailNodesResponse(
                            row.id(),
                            row.parentId(),
                            row.directoryNodeId(),
                            row.label(),
                            objectStorageService.generatePresignedUrl(row.imageUrl()),
                            row.x(),
                            row.y(),
                            row.imageWidth(),
                            row.imageHeight(),
                            row.contentType(),
                            row.article(),
                            row.markType(),
                            row.createdAt().toInstant(ZoneOffset.UTC),
                            row.updatedAt().toInstant(ZoneOffset.UTC)
                    );
                }).toList();
    }

    // 图片节点的文件存 vps 本地
    public BatchUpdateDetailNodesResponse oldBatchUpdateDetailNotes(
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
            FetchDetailNodesRow existingNode =
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
            BatchUpdateDetailNodesTransactionResult result = detailNodeTransactionService.batchUpdateDetailNotesToDB(directoryNodeId, request, succeededImageNodes, deletedIdsToBeVerified);

            // 删除 deleted 节点 相关图片节点对应的文件
            oldDeleteFilesOfImageNodes(result.deletedImageObjectKeys());

            return result.response();
        } catch (RuntimeException e) {
            // 清理已保存的文件
            fileService.deleteQuietly(filesToBeDeletedWhenError);
            throw e;
        }
    }

    // 图片节点的文件存 对象存储
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

        // 校验：added 的节点的 UUID 是否唯一
        Set<UUID> addedUuids = new HashSet<>();
        for (BatchCreateDetailNodesDTO addedNodes : request.added()) {
            if (!addedUuids.add(addedNodes.getUuid())) {
                throw new BusinessException(ErrorCode.INVALID_DETAIL_NODE_RELATION);
            }
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
            FetchDetailNodesRow existingNode =
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
        List<String> filesToBeDeletedWhenError = new ArrayList<>();

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

                String objectKey = "imageNodes/" + userId + "/" + filename;

                try {
                    objectStorageService.upload(file, objectKey);
                } catch (BusinessException e) {
                    objectStorageService.deleteQuietly(objectKey);

                    throw e;
                }

                vo.setUrl(objectKey);
                filesToBeDeletedWhenError.add(objectKey);
                succeededImageNodes.put(vo.getUuid(), vo);
            }

            // 再更新数据库
            BatchUpdateDetailNodesTransactionResult res = detailNodeTransactionService.batchUpdateDetailNotesToDB(directoryNodeId, request, succeededImageNodes, deletedIdsToBeVerified);

            // 删除 deleted 节点 相关图片节点对应的文件
            deleteFilesOfImageNodes(res.deletedImageObjectKeys());

            return res.response();
        } catch (RuntimeException e) {
            // 清理已保存的文件
            objectStorageService.deleteQuietly(filesToBeDeletedWhenError);
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

    public void hardDeleteFromTrashBin(Long userId, IdsRequest request) {
        List<FindImageNodesByDirectoryNodesWithDetailResponse> imageNodesInvolved = noteTransactionService.hardDeleteRootNodesFromTrashBin(userId, request.ids());

        deleteFilesOfImageNodes(imageNodesInvolved.stream().map(FindImageNodesByDirectoryNodesWithDetailResponse::imageUrl).toList());
    }

    // 删除图片节点对应的文件（vps 本地存储）
    private void oldDeleteFilesOfImageNodes(List<String> imageUrls) {
        List<Path> paths = imageUrls.stream().map(url -> {
            String fileUrl = url.substring(url.indexOf("imageNodes/"));
            Path dir = Paths.get(fileUploadProperties.uploadDir());
            return dir.resolve(fileUrl);
        }).toList();
        fileService.deleteQuietly(paths);
    }

    // 删除图片节点对应的文件（对象存储）
    private void deleteFilesOfImageNodes(List<String> imageUrls) {
        objectStorageService.deleteQuietly(imageUrls);
    }

    public CleanUpDeletedRootNodesResponse cleanUpDeletedRootNodes() {
        CleanUpDeletedRootNodesResponse response = noteTransactionService.cleanUpDeletedRootNodes();

        deleteFilesOfImageNodes(response.imageNodes().stream().map(FindImageNodesByDirectoryNodesWithDetailResponse::imageUrl).toList());

        return response;
    }

    // 移动 DETAIL 目录节点 A 下的详情节点 X（包括后代节点）
    // 到 DETAIL 目录节点 B 下，作为一个独立节点/树
    @Transactional
    public void moveDetailNode(Long userId, Long detailNodeId, Long oldDirectoryNodeId, Long destDirectoryNodeId) {
        if (Objects.equals(oldDirectoryNodeId, destDirectoryNodeId)) {
            throw new BusinessException(ErrorCode.OLD_AND_DEST_DIRECTORY_NODES_ARE_SAME_CODE);
        }

        // 校验用户有该 oldDirectoryNode
        if (!directoryNodeMapper.hasDirectoryNode(userId, oldDirectoryNodeId, NodeType.DETAIL)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }

        // 校验用户有该详情节点
        FetchDetailNodesRow detailNodesRow = detailNodeMapper.getDetailNodeById(detailNodeId, oldDirectoryNodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DETAIL_NODE_NOT_FOUND));

        // 校验用户有该 destDirectoryNode
        if (!directoryNodeMapper.hasDirectoryNode(userId, destDirectoryNodeId, NodeType.DETAIL)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }

        // 更新 X 和所有后代的 directory_node_id 和 x/y，并更新 X 的 parentId 为 null
        BigDecimal offsetX = detailNodesRow.x();
        BigDecimal offsetY = detailNodesRow.y();

        int affectedRows = detailNodeMapper.moveDetailNode(detailNodeId, oldDirectoryNodeId, destDirectoryNodeId, offsetX, offsetY);
        if (affectedRows < 1) {
            throw new BusinessException(ErrorCode.DETAIL_NODE_MOVE_FAILED);
        }
    }

    public List<TagResponse> getTagsByDirectoryNodeId(Long userId, Long directoryNodeId) {
        // 校验 用户有该节点
        if (!directoryNodeMapper.hasDirectoryNode(userId, directoryNodeId, NodeType.DETAIL)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }

        return directoryNodeTagMapper.getTagsByDirectoryNodeId(directoryNodeId).stream()
                .map(t -> new TagResponse(t.id(), t.name(), t.createdAt().toInstant(ZoneOffset.UTC)))
                .toList();
    }

    public String shareNote(Long userId, Long directoryNodeId) {
        // --- 校验
        // 用户有该节点
        if (!directoryNodeMapper.hasDirectoryNode(userId, directoryNodeId, NodeType.DETAIL)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }

        // 生成 token
        String shareToken = UUID.randomUUID().toString();

        // 尝试找分享记录
        Optional<NoteShareRow> optionalNoteShare = directoryNodeMapper.findNoteShareByDirectoryNodeId(directoryNodeId);
        if (optionalNoteShare.isPresent()) {
            // 已存在分享记录

            // 如果 status 为 active 则返回已有 token
            if (optionalNoteShare.get().status() == NoteShareStatus.ACTIVE) {
                return optionalNoteShare.get().shareToken();
            }

            // 更新状态
            if (directoryNodeMapper.activateNoteShare(directoryNodeId, shareToken) != 1) {
                throw new BusinessException(ErrorCode.UPDATE_NOTE_SHARE_FAILED);
            }
        } else {
            // 新增分享记录
            if (directoryNodeMapper.createNoteShare(directoryNodeId, shareToken) != 1) {
                throw new BusinessException(ErrorCode.CREATE_NOTE_SHARE_FAILED);
            }
        }

        return shareToken;
    }

    public void unshareNote(Long userId, Long directoryNodeId) {
        // 校验 用户有该节点
        if (!directoryNodeMapper.hasDirectoryNode(userId, directoryNodeId, NodeType.DETAIL)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }

        // 尝试找分享记录
        NoteShareRow noteShareRow = directoryNodeMapper.findNoteShareByDirectoryNodeId(directoryNodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTE_SHARE_NOT_FOUND));

        // 如果 status 为 active 则返回
        if (noteShareRow.status() == NoteShareStatus.REVOKED) {
            throw new BusinessException(ErrorCode.NOTE_HAS_ALREADY_REVOKED);
        }

        if (directoryNodeMapper.cancelNoteShare(directoryNodeId) != 1) {
            throw new BusinessException(ErrorCode.UPDATE_NOTE_SHARE_FAILED);
        }
    }

    public GetDetailNodesByShareTokenResponse getDetailNodesByShareToken(Long userId, String shareToken, String visitorId) {
        // 校验 shareToken、status
        NoteShareRow noteShareRow = directoryNodeMapper.findNoteShareByShareToken(shareToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTE_SHARE_NOT_FOUND));

        // 查 directory node
        List<DirectoryNodeWithTagRow> rows = directoryNodeMapper.getDirectoryNodesByIds(List.of(noteShareRow.directoryNodeId()));
        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }

        // 组装 title 和 tags 响应字段
        String title = rows.getFirst().label();
        Set<SimpleTagResponse> tags = rows.stream()
                .filter(row -> row.tagId() != null)
                .map(row -> new SimpleTagResponse(
                        row.tagId(),
                        row.tagName()
                ))
                .collect(Collectors.toSet());

        // 查 detail nodes
        List<FetchDetailNodesResponse> detailNodes = detailNodeMapper.fetchDetailNodesByDirectoryNodeId(noteShareRow.directoryNodeId()).stream()
                .map(row -> {
                    return new FetchDetailNodesResponse(
                            row.id(),
                            row.parentId(),
                            row.directoryNodeId(),
                            row.label(),
                            objectStorageService.generatePresignedUrl(row.imageUrl()),
                            row.x(),
                            row.y(),
                            row.imageWidth(),
                            row.imageHeight(),
                            row.contentType(),
                            row.article(),
                            row.markType(),
                            row.createdAt().toInstant(ZoneOffset.UTC),
                            row.updatedAt().toInstant(ZoneOffset.UTC)
                    );
                }).toList();

        // owner 不产生访问记录
        if (userId == null || !directoryNodeMapper.hasDirectoryNode(userId, noteShareRow.directoryNodeId(), NodeType.DETAIL)) {
            // 记录访问
            noteShareAccessLogAsyncService.createNoteShareAccessLog(noteShareRow.id(), NoteShareTarget.NOTE, noteShareRow.directoryNodeId(), visitorId);
        }

        return new GetDetailNodesByShareTokenResponse(
                title,
                tags,
                detailNodes
        );
    }

    public NoteShareResponse getNoteShareByDirectoryNodeId(Long userId, Long directoryNodeId) {
        // 校验 用户有该节点
        if (!directoryNodeMapper.hasDirectoryNode(userId, directoryNodeId, NodeType.DETAIL)) {
            throw new BusinessException(ErrorCode.DIRECTORY_NODE_NOT_FOUND);
        }

        // 尝试找分享记录
        NoteShareRow noteShareRow = directoryNodeMapper.findNoteShareByDirectoryNodeId(directoryNodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTE_SHARE_NOT_FOUND));

        return NoteShareResponse.from(noteShareRow);
    }

    public List<NoteShareAccessLogResponse> fetchNoteShareAccessLogsByNoteShareId(Long userId, Long noteShareId) {
        // 校验
        if (!directoryNodeMapper.hasUserSharedNote(userId, noteShareId)) {
            throw new BusinessException(ErrorCode.NOTE_SHARE_NOT_FOUND);
        }

        // 尝试找分享记录
        List<NoteShareAccessLogRow> rows = directoryNodeMapper.fetchNoteShareAccessLogsByNoteShareId(noteShareId);

        return rows.stream()
                .map(NoteShareAccessLogResponse::from)
                .toList();
    }

    public FetchArticleResponse getArticleByDetailNodeId(Long userId, Long detailNodeId) {
        return FetchArticleResponse.from(articleMapper.getArticleByUserIdAndDetailNodeId(userId, detailNodeId).orElseThrow(()->new BusinessException(ErrorCode.ARTICLE_NOT_FOUND)));
    }

    public void updateArticleByDetailNodeId(Long userId, Long detailNodeId, String content) {
        if(articleMapper.updateArticleByDetailNodeId(userId, detailNodeId, content) != 1) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
    }

    public FetchArticleResponse getArticleByDetailNodeIdAndShareToken(Long userId, Long detailNodeId, String shareToken, String visitorId){
        // 查 article
        GetNoteShareArticleRow row = articleMapper.getNoteShareArticle(detailNodeId, shareToken).orElseThrow(()->new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        // owner 不产生访问记录
        if (userId == null || !directoryNodeMapper.hasDirectoryNode(userId, row.directoryNodeId(), NodeType.DETAIL)) {
            // 记录访问
            noteShareAccessLogAsyncService.createNoteShareAccessLog(row.noteShareId(), NoteShareTarget.ARTICLE, detailNodeId, visitorId);
        }

        return new FetchArticleResponse(
                row.id(),
                row.content(),
                row.createdAt().toInstant(ZoneOffset.UTC),
                row.updatedAt().toInstant(ZoneOffset.UTC)
        );
    }
}
