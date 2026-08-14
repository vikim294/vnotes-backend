package com.vikim.demo.note;

import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImportService {
    private final ObjectMapper objectMapper;
    private final ImportTransactionService importTransactionService;

    @Transactional
    public void importJsonNotes(Long userId, MultipartFile file) {
        ImportJsonNotesDTO data = null;

        try {
            data = objectMapper.readValue(
                            file.getInputStream(),
                            ImportJsonNotesDTO.class
                    );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.READ_IMPORTED_FILE_FAILED);
        }

        // TODO 校验
        if(!"1".equals(data.version())) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_VERSION);
        }

        if(data.directoryNodes() == null || data.detailNodes() == null) {
            throw new BusinessException(ErrorCode.READ_IMPORTED_FILE_FAILED);
        }

        // 构建 map，加速“从 list 中遍历找 parentId 的子节点”
        Map<Long, List<ImportDirectoryNodeDTO>> directoryNodeParentIdMap = new HashMap<>();

        for (ImportDirectoryNodeDTO directoryNodeDTO : data.directoryNodes()) {
            directoryNodeParentIdMap.computeIfAbsent(directoryNodeDTO.sourceParentId(), ignored -> new ArrayList<>())
                    .add(directoryNodeDTO);
        }

        Map<Long, Long> directoryNodeSourceIdToIdMap = new HashMap<>();
        Map<Long, Long> detailNodeSourceIdToIdMap = new HashMap<>();

        // 先递归创建 目录节点
        importTransactionService.createDescendentDirectoryNodesOfId(
                userId,
                null,
                directoryNodeParentIdMap,
                directoryNodeSourceIdToIdMap
        );

        // 再递归创建 详情节点
        List<ImportDirectoryNodeDTO> directoryNodesWithDetail = data.directoryNodes().stream()
                .filter(d -> d.nodeType() == NodeType.DETAIL)
                .toList();

        // 创建每个带详情的 目录节点 的详情
        for (ImportDirectoryNodeDTO directoryNodeWithDetail : directoryNodesWithDetail) {
            importTransactionService.createDescendentDetailNodesOfId(
                    directoryNodeWithDetail.sourceId(),
                    directoryNodeSourceIdToIdMap.get(directoryNodeWithDetail.sourceId()),
                    null,
                    data.detailNodes(),
                    detailNodeSourceIdToIdMap
            );
        }
    }
}
