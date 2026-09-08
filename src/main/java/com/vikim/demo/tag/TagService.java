package com.vikim.demo.tag;

import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;

    public void createTag(Long userId, String name){
        int affectedRows = tagMapper.createTag(userId, name);
        if(affectedRows != 1) {
            throw new BusinessException(ErrorCode.CREATE_TAG_FAILED);
        }
    }

    public List<TagResponse> fetchTagsByUserId(Long userId){
        return tagMapper.fetchTagsByUserId(userId).stream()
                .map(t-> new TagResponse(t.id(), t.name(), t.createdAt().toInstant(ZoneOffset.UTC)))
                .toList();
    }

    public void updateTagName(Long userId, Long id, String name){
        int affectedRows = tagMapper.updateTagName(userId, id , name);
        if(affectedRows != 1) {
            throw new BusinessException(ErrorCode.UPDATE_TAG_FAILED);
        }
    }

    public void deleteTag(Long userId, Long id){
        int affectedRows = tagMapper.deleteTag(userId, id);
        if(affectedRows != 1) {
            throw new BusinessException(ErrorCode.DELETE_TAG_FAILED);
        }
    }
}
