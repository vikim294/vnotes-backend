package com.vikim.demo.note;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteShareAccessLogAsyncService {

    private final DirectoryNodeMapper directoryNodeMapper;

    @Async("logTaskExecutor")
    public void createNoteShareAccessLog(Long noteShareId) {
        try {
            directoryNodeMapper.createNoteShareAccessLog(noteShareId);
        } catch (Exception e) {
            log.error("异步记录分享笔记访问失败, noteShareId={}", noteShareId, e);
        }
    }
}
