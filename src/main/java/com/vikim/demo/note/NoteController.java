package com.vikim.demo.note;

import com.vikim.demo.common.ApiResponse;
import com.vikim.demo.common.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/note")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    private Long getIdFromAuthentication(
            Authentication authentication
    ) {
        return Long.valueOf(authentication.getName());
    }

    @PostMapping("/directory")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> createDirectoryNode(
            Authentication authentication,
            @Valid @RequestBody CreateDirectoryNodeRequest request
    ) {
        return ApiResponse.success(noteService.createDirectoryNode(getIdFromAuthentication(authentication), request), "创建目录节点成功");
    }

    @GetMapping("/directory")
    public ApiResponse<List<FetchDirectoryNodesResponse>> fetchAllDirectoryNodes(
            Authentication authentication
    ) {
        return ApiResponse.success(noteService.fetchAllDirectoryNodes(getIdFromAuthentication(authentication)), "获取笔记目录成功");
    }

    @GetMapping("/directory/search")
    public ApiResponse<PageResponse<FetchDirectoryNodesResponse>> searchDirectoryNodes(
            Authentication authentication,

            @RequestParam(value = "keyword", required = false)
            String keyword,

            @RequestParam(value = "pageSize", defaultValue = "10")
            @Min(value = 1, message = "pageSize 不能小于 1")
            @Max(value = 100, message = "pageSize 不能大于 100")
            int pageSize,

            @Min(value = 1, message = "page 不能小于 1")
            @RequestParam(value = "page", defaultValue = "1")
            int page,

            @RequestParam(value = "nodeType", required = false)
            NodeType nodeType,

            @RequestParam(value = "startTime", required = false)
            LocalDateTime startTime,

            @RequestParam(value = "endTime", required = false)
            LocalDateTime endTime
    ) {
        return ApiResponse.success(noteService.searchDirectoryNodes(getIdFromAuthentication(authentication), keyword, pageSize, page, nodeType, startTime, endTime), "搜索笔记目录成功");
    }

    @DeleteMapping("/directory/{id}")
    public ApiResponse<Void> deleteDirectoryNode(
            Authentication authentication,
            @PathVariable("id") Long id
    ) {
        noteService.deleteDirectoryNode(id, getIdFromAuthentication(authentication));
        return ApiResponse.success("删除目录节点成功");
    }

    @PatchMapping("/directory/batch")
    public ApiResponse<BatchUpdateDirectoryNodesResponse> batchUpdateDirectoryNotes(
            Authentication authentication,
            @Valid @RequestBody BatchUpdateDirectoryNodesRequest request
    ) {
        return ApiResponse.success(noteService.batchUpdateDirectoryNotes(getIdFromAuthentication(authentication), request), "批量更新目录节点成功");
    }

    @GetMapping("/directory/{id}/detail-nodes")
    public ApiResponse<List<FetchDetailNodesResponse>> fetchDetailNodesByDirectoryNodeId(
            Authentication authentication,
            @PathVariable("id") Long id
    ) {
        return ApiResponse.success(noteService.fetchDetailNodesByDirectoryNodeId(getIdFromAuthentication(authentication), id), "获取笔记详情成功");
    }

    @PatchMapping(
            value = "/directory/{id}/detail-nodes/batch",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<BatchUpdateDetailNodesResponse> batchUpdateDetailNotes(
            Authentication authentication,
            @PathVariable("id") Long id,
            // 前端需要让 request 使用 application/json 类型的 Blob；如果用普通字符串，@RequestPart 可能因为 part 的 Content-Type 变成 text/plain 而无法自动反序列化 DTO
            @Valid @RequestPart("request") BatchUpdateDetailNodesRequest request,
            MultipartHttpServletRequest multipartRequest
    ) {
        // Blob 在 multipart 中也会被 Spring 当作文件 part
        // 因此：multipartRequest.getFileMap() 拿到的不只是图片，还包括 request
        // 所以这里先把图片文件都过滤出来
        Map<String, MultipartFile> imageFiles =
                multipartRequest.getFileMap()
                        .entrySet()
                        .stream()
                        .filter(entry ->
                                entry.getKey().startsWith("file.")
                        )
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        ));
        BatchUpdateDetailNodesResponse response = noteService.batchUpdateDetailNotes(getIdFromAuthentication(authentication), id, request, imageFiles);
        return ApiResponse.success(response, "批量更新详情节点成功");
    }
}
