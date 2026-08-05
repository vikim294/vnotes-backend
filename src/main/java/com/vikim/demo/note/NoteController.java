package com.vikim.demo.note;

import com.vikim.demo.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/note")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
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

    @DeleteMapping("/directory/{id}")
    public ApiResponse<Void> deleteDirectoryNode(
            Authentication authentication,
            @PathVariable("id") Long id
    ) {
        noteService.deleteDirectoryNode(id, getIdFromAuthentication(authentication));
        return ApiResponse.success("删除目录节点成功");
    }

    @PostMapping("/directory/batch")
    public ApiResponse<BatchCreateDirectoryNodesResponse> batchCreateDirectoryNotes(
            Authentication authentication,
            @Valid @RequestBody BatchCreateDirectoryNodesRequest request
    ) {
        return ApiResponse.success(noteService.batchCreateDirectoryNotes(getIdFromAuthentication(authentication), request), "批量新增目录节点成功");
    }

    private Long getIdFromAuthentication(
            Authentication authentication
    ) {
        return Long.valueOf(authentication.getName());
    }
}
