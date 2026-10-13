package com.vikim.vnotes.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventMapper outboxEventMapper;

    @Transactional
    public List<OutboxEvent> pickJobs(){
        // 查询并加悲观锁
        List<OutboxEvent> outboxEvents = outboxEventMapper.fetchPendingOutboxEventsForUpdate();

        if(outboxEvents.isEmpty()) return List.of();

        // 领取任务
        outboxEventMapper.pickJobs(outboxEvents.stream().map(OutboxEvent::getEventId).toList());

        // 领取任务时会将 claimVersion + 1，所以上面查出来的列表 需要更新该字段：
        outboxEvents.forEach(item -> item.setClaimVersion(item.getClaimVersion() + 1));

        return outboxEvents;
    }
}
