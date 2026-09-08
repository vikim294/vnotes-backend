package com.vikim.demo.tag;

import com.vikim.demo.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tag")
public class TagController {

    private final TagService tagService;

    private Long getIdFromAuthentication(
            Authentication authentication
    ) {
        return Long.valueOf(authentication.getName());
    }

    @PostMapping
    public ApiResponse<Void> createTag(
            Authentication authentication,
            @Valid @RequestBody CreateTagRequest request
    ){
        tagService.createTag(getIdFromAuthentication(authentication), request.name());
        return ApiResponse.success("创建标签成功");
    }


    @GetMapping
    public ApiResponse<List<TagResponse>> fetchTagsByUserId(
            Authentication authentication
    ){
        return ApiResponse.success(tagService.fetchTagsByUserId(getIdFromAuthentication(authentication)),"获取标签列表成功");
    }

    @PatchMapping
    public ApiResponse<Void> updateTagName(
            Authentication authentication,
            @Valid @RequestBody UpdateTagRequest request
    ){
        tagService.updateTagName(getIdFromAuthentication(authentication), request.id(), request.name());
        return ApiResponse.success("更新标签成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTag(
            Authentication authentication,
            @PathVariable("id") Long id
    ){
        tagService.deleteTag(getIdFromAuthentication(authentication), id);
        return ApiResponse.success("删除标签成功");
    }
}
