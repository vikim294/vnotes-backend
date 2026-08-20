package com.vikim.demo.cron;

import com.vikim.demo.note.CleanUpDeletedRootNodesResponse;
import com.vikim.demo.note.NoteService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoteCronJobs {

    private final NoteService noteService;
    private static final Logger log =
            LoggerFactory.getLogger(NoteCronJobs.class);

    // 每天凌晨清理软删除 > 30天的 deletedRootNode
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Shanghai")
    public void cleanUpDeletedRootNodes() {
        log.info("cron job：回收站定时清理开始");

        CleanUpDeletedRootNodesResponse response = noteService.cleanUpDeletedRootNodes();

        log.info("cron job：回收站定时清理结束，删除根节点数量：{}，尝试删除图片文件数量：{}",
                response.deletedRootNum(),
                response.imageNodes().size()
        );
    }
}
