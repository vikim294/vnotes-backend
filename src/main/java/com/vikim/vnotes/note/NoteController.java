package com.vikim.vnotes.note;

import com.vikim.vnotes.common.ApiResponse;
import com.vikim.vnotes.common.PageResponse;
import com.vikim.vnotes.tag.TagResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/note")
public class NoteController {
    private final NoteService noteService;
    private final ExportService exportService;
    private final ImportService importService;
    private final ObjectMapper objectMapper;
    @Value("${app.cookie.secure:false}")
    boolean cookieSecure;

    public NoteController(NoteService noteService, ExportService exportService, ImportService importService, ObjectMapper objectMapper) {
        this.noteService = noteService;
        this.exportService = exportService;
        this.importService = importService;
        this.objectMapper = objectMapper;
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
    public ApiResponse<PageResponse<FetchDirectoryNodesWithTagsResponse>> searchDirectoryNodes(
            Authentication authentication,

            @RequestParam(value = "keyword", required = false)
            @Size(max = 255, message = "关键词最多 255 字符")
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

            @RequestParam(value = "tagIds", required = false)
            @Size(max = 100, message = "tagIds 不能多于 100")
            Set<@NotNull Long> tagIds,

            @RequestParam(value = "startTime", required = false)
            LocalDateTime startTime,

            @RequestParam(value = "endTime", required = false)
            LocalDateTime endTime
    ) {
        return ApiResponse.success(noteService.searchDirectoryNodes(getIdFromAuthentication(authentication), keyword, pageSize, page, nodeType, tagIds, startTime, endTime), "搜索笔记目录成功");
    }

    @GetMapping("/directory/trash-bin/search")
    public ApiResponse<PageResponse<SearchDirectoryNodesInTrashBinResponse>> searchDirectoryNodesInTrashBin(
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
        return ApiResponse.success(noteService.searchDirectoryNodesInTrashBin(getIdFromAuthentication(authentication), keyword, pageSize, page, nodeType, startTime, endTime), "搜索回收站笔记目录成功");
    }

    @PatchMapping("/directory/batch")
    public ApiResponse<BatchUpdateDirectoryNodesResponse> batchUpdateDirectoryNotes(
            Authentication authentication,
            @Valid @RequestBody BatchUpdateDirectoryNodesRequest request
    ) {
        return ApiResponse.success(noteService.batchUpdateDirectoryNotes(getIdFromAuthentication(authentication), request), "批量更新目录节点成功");
    }

    @GetMapping("/directory/{id}/detail-nodes")
    public ApiResponse<FetchDetailNodesByDirectoryNodeIdResponse> fetchDetailNodesByDirectoryNodeId(
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

    @GetMapping("/export/json")
    public ResponseEntity<byte[]> exportNotesToJson(
            Authentication authentication
    ) {
        ExportNotesToJsonResponse response = exportService.exportNotesToJson(getIdFromAuthentication(authentication));

        byte[] bytes = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(response);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"notes.json\""
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(bytes);
    }

    @PostMapping(value = "/import/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> importJsonNotes(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "strategy", defaultValue = "APPEND") ImportStrategy strategy
    ) {
        importService.importJsonNotes(getIdFromAuthentication(authentication), strategy, file);
        return ApiResponse.success("导入 json 笔记成功");
    }

    @PatchMapping("/directory/trash-bin/batch-restore")
    public ApiResponse<Void> restoreDirectoryNodesFromTrashBinByIds(
            Authentication authentication,
            @Valid @RequestBody IdsRequest request
    ) {
        noteService.restoreDirectoryNodesFromTrashBinByIds(getIdFromAuthentication(authentication), request);
        return ApiResponse.success("批量恢复目录节点成功");
    }

    @PostMapping("/directory/trash-bin/batch-delete")
    public ApiResponse<Void> hardDeleteFromTrashBin(
            Authentication authentication,
            @Valid @RequestBody IdsRequest request
    ) {
        noteService.hardDeleteFromTrashBin(getIdFromAuthentication(authentication), request);
        return ApiResponse.success("批量彻底删除成功");
    }

    @PostMapping("/directory/{id}/detail-nodes/move")
    public ApiResponse<Long> moveDetailNode(
            Authentication authentication,
            @PathVariable("id") Long oldDirectoryNodeId,
            @Valid @RequestBody MoveDetailNodeRequest request
    ) {
        noteService.moveDetailNode(getIdFromAuthentication(authentication), request.detailNodeId(), oldDirectoryNodeId, request.destDirectoryNodeId(), request.sourceVersion());
        return ApiResponse.success(request.sourceVersion() + 1, "移动详情节点成功");
    }

    @GetMapping("/directory/{id}/tag")
    public ApiResponse<List<TagResponse>> getTagsByDirectoryNodeId(
            Authentication authentication,
            @PathVariable("id") Long directoryNodeId
    ) {
        return ApiResponse.success(noteService.getTagsByDirectoryNodeId(getIdFromAuthentication(authentication), directoryNodeId), "获取笔记标签成功");
    }

    @PostMapping("/share")
    public ApiResponse<String> shareNote(
            Authentication authentication,
            @RequestParam("directoryNodeId")
            Long directoryNodeId
    ) {
        return ApiResponse.success(noteService.shareNote(getIdFromAuthentication(authentication), directoryNodeId), "分享笔记成功");
    }

    @PatchMapping("/share")
    public ApiResponse<Void> unshareNote(
            Authentication authentication,
            @RequestParam("directoryNodeId") Long directoryNodeId
    ) {
        noteService.unshareNote(getIdFromAuthentication(authentication), directoryNodeId);
        return ApiResponse.success("取消分享成功");
    }

    @GetMapping("/shared/note/{shareToken}")
    public ResponseEntity<ApiResponse<GetDetailNodesByShareTokenResponse>> getDetailNodesByShareToken(
            Authentication authentication,
            @PathVariable("shareToken") String shareToken,
            @CookieValue(value = "visitorId", required = false)
            String visitorId
    ) {
        // 处理匿名用户访问接口
        Long userId = null;

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            userId = getIdFromAuthentication(authentication);
        }

        // 处理 cookie
        boolean newVisitor = isNewVisitor(visitorId);
        if (newVisitor) {
            visitorId = UUID.randomUUID().toString();
        }

        // 调 service
        GetDetailNodesByShareTokenResponse response = noteService.getDetailNodesByShareToken(userId, shareToken, visitorId);

        ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.ok();

        // 设置响应头 set-cookie
        if (newVisitor) {
            ResponseCookie cookie = ResponseCookie
                    .from("visitorId", visitorId)
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofDays(365))
                    .build();

            bodyBuilder.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return bodyBuilder.body(ApiResponse.success(response, "获取笔记成功"));
    }

    @GetMapping("/share")
    public ApiResponse<NoteShareResponse> getNoteShareByDirectoryNodeId(
            Authentication authentication,
            @RequestParam("directoryNodeId") Long directoryNodeId
    ) {
        return ApiResponse.success(noteService.getNoteShareByDirectoryNodeId(getIdFromAuthentication(authentication), directoryNodeId), "获取分享详情成功");
    }

    @GetMapping("/share/access-logs/{id}")
    public ApiResponse<FetchNoteShareAccessLogsByNoteShareIdResponse> fetchNoteShareAccessLogsByNoteShareId(
            Authentication authentication,
            @PathVariable("id") Long noteShareId,
            @RequestParam(value = "cursor", required = false)
            String cursor
    ) {
        return ApiResponse.success(noteService.fetchNoteShareAccessLogsByNoteShareId(getIdFromAuthentication(authentication), noteShareId, cursor), "获取分享访问记录成功");
    }

    @GetMapping("/article/{detailNodeId}")
    public ApiResponse<FetchArticleResponse> getArticleByDetailNodeId(
            Authentication authentication,
            @PathVariable("detailNodeId") Long detailNodeId
    ) {
        return ApiResponse.success(noteService.getArticleByDetailNodeId(getIdFromAuthentication(authentication), detailNodeId), "获取文章成功");
    }

    @PatchMapping("/article/{detailNodeId}")
    public ApiResponse<Void> updateArticleByDetailNodeId(
            Authentication authentication,
            @PathVariable("detailNodeId") Long detailNodeId,
            @RequestBody UpdateArticleRequest request
    ) {
        noteService.updateArticleByDetailNodeId(getIdFromAuthentication(authentication), detailNodeId, request.content());
        return ApiResponse.success("更新文章成功");
    }

    private boolean isNewVisitor(String visitorId) {
        if (visitorId == null || visitorId.isBlank()) {
            return true;
        } else {
            try {
                UUID.fromString(visitorId);
                return false;
            } catch (IllegalArgumentException e) {
                return true;
            }
        }
    }

    @GetMapping("/shared/article/{detailNodeId}/{shareToken}")
    public ResponseEntity<ApiResponse<FetchArticleResponse>> getArticleByDetailNodeIdAndShareToken(
            Authentication authentication,
            @PathVariable("detailNodeId") Long detailNodeId,
            @PathVariable("shareToken") String shareToken,
            @CookieValue(value = "visitorId", required = false)
            String visitorId
    ) {
        // 处理匿名用户访问接口
        Long userId = null;

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            userId = getIdFromAuthentication(authentication);
        }

        // 处理 cookie
        boolean newVisitor = isNewVisitor(visitorId);
        if (newVisitor) {
            visitorId = UUID.randomUUID().toString();
        }

        // 调 service
        FetchArticleResponse response = noteService.getArticleByDetailNodeIdAndShareToken(userId, detailNodeId, shareToken, visitorId);

        ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.ok();

        // 设置响应头 set-cookie
        if (newVisitor) {
            ResponseCookie cookie = ResponseCookie
                    .from("visitorId", visitorId)
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofDays(365))
                    .build();

            bodyBuilder.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return bodyBuilder.body(ApiResponse.success(response, "获取文章成功"));
    }

    @GetMapping("/directory/{id}")
    public ApiResponse<GetDirectoryNodeByIdResponse> getDirectoryNodeById(
            Authentication authentication,
            @PathVariable("id") Long id
    ) {
        return ApiResponse.success(noteService.getDirectoryNodeById(getIdFromAuthentication(authentication), id), "获取笔记信息成功");
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<GetDetailNodeLocationResponse> getDetailNodeLocation(
            Authentication authentication,
            @PathVariable("id") Long id
    ) {
        return ApiResponse.success(noteService.getDetailNodeLocation(getIdFromAuthentication(authentication), id), "获取详情节点信息成功");
    }

    @GetMapping("/search-history")
    public ApiResponse<List<NoteSearchHistoryResponse>> getUserNoteSearchHistory(
            Authentication authentication
    ) {
        return ApiResponse.success(noteService.getUserNoteSearchHistory(getIdFromAuthentication(authentication)), "获取笔记搜索历史成功");
    }

    @PostMapping("/directory/{id}/access")
    public ApiResponse<Void> createNoteAccessRecord(
            Authentication authentication,
            @PathVariable("id") Long id
    ) {
        noteService.createNoteAccessRecord(getIdFromAuthentication(authentication), id);
        return ApiResponse.success("创建笔记访问记录成功");
    }

    @GetMapping("/directory/recent-accesses")
    public ApiResponse<List<GetRecentAccessedNoteHistoryResponse>> getRecentAccessedNoteHistory(
            Authentication authentication
    ) {
        return ApiResponse.success(noteService.getRecentAccessedNoteHistory(getIdFromAuthentication(authentication)), "获取最近访问笔记历史成功");
    }
}
