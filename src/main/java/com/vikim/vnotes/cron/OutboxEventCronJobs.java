package com.vikim.vnotes.cron;

import com.vikim.vnotes.common.VnotesEvent;
import com.vikim.vnotes.mq.MessageProducer;
import com.vikim.vnotes.mq.events.UserRegisteredEvent;
import com.vikim.vnotes.outbox.OutboxEvent;
import com.vikim.vnotes.outbox.OutboxEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxEventCronJobs {

    private final OutboxEventMapper outboxEventMapper;
    private final MessageProducer messageProducer;
    private final ObjectMapper objectMapper;

    // 轮询 outbox_events，找出 pending 状态的，然后发 mq
    @Scheduled(fixedDelay = 5000, zone = "Asia/Shanghai")
    public void scanAndSendEventRegularly() {
        log.info("cron job：轮询 outbox_events 发 mq");
//         添加一个 SENDING 状态，多实例下，实例 1 先找 10 条 PENDING，并更新状态为 SENDING
//         这样实例 2 查到的就是另 10 条 PENDING
//         为了保证实例 1 查询并更新状态是原子操作，是不是需要 select... for update？状态更新后结束事务，释放锁
//         什么时候 SENDING 恢复回 PENDING 状态？解析 json 时出错？发 mq 过程中出现 nAck 或 return 时？
        List<OutboxEvent> pendingOutboxEvents = outboxEventMapper.fetchPendingOutboxEvents();

        for (OutboxEvent event: pendingOutboxEvents) {
            switch (event.getEventName()) {
                // 用户注册成功
                case VnotesEvent.USER_REGISTERED -> {
                    // todo 处理可能的 json 解析异常
                    // todo 添加重试 和 failed 闭环

                    // 解析 event json
                    UserRegisteredEvent userRegisteredEvent = objectMapper.readValue(event.getEventJson(), UserRegisteredEvent.class);

                    // 发 mq
                    messageProducer.sendUserRegisteredEvent(new UserRegisteredEvent(
                            event.getEventId(),
                            userRegisteredEvent.userId()
                    ));
                }

                default -> {

                }
            }
        }
    }
}
