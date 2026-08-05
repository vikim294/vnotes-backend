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

    public NoteController(NoteService noteService){
        this.noteService = noteService;
    }

    @PostMapping("/directory/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> createDirectoryNode(
            Authentication authentication,
            @Valid @RequestBody CreateDirectoryNodeRequest request
    ) {
        noteService.createDirectoryNode(Long.valueOf(authentication.getName()), request);
        return ApiResponse.success("创建目录节点成功");
    }

    @GetMapping("/directory")
    public ApiResponse<List<FetchDirectoryNodesResponse>> fetchAllDirectoryNodes(
            Authentication authentication
    ) {
        return ApiResponse.success(noteService.fetchAllDirectoryNodes(Long.valueOf(authentication.getName())), "获取笔记目录成功");
    }
}
