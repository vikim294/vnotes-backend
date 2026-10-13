package com.vikim.vnotes.cron;

import com.vikim.vnotes.common.VnotesEvent;
import com.vikim.vnotes.mq.MessageProducer;
import com.vikim.vnotes.mq.events.UserRegisteredEvent;
import com.vikim.vnotes.outbox.OutboxEvent;
import com.vikim.vnotes.outbox.OutboxEventMapper;
import com.vikim.vnotes.outbox.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxEventCronJobs {

    private final OutboxEventService outboxEventService;
    private final OutboxEventMapper outboxEventMapper;
    private final MessageProducer messageProducer;
    private final ObjectMapper objectMapper;

    // 轮询 outbox_events，找出 pending 状态的，然后发 mq
    @Scheduled(fixedDelay = 5000, zone = "Asia/Shanghai")
    public void scanAndSendEventRegularly() {
        log.info("cron job：轮询 outbox_events 发 mq");

        /*
            添加一个 SENDING 状态，多实例下，实例 1 先找 10 条 PENDING，并更新状态为 SENDING，这样实例 2 查到的就是另 10 条 PENDING
            为了保证实例 1 查询并更新状态是原子操作，
            先在一个 Bean 内使用 select... for update skip locked 找 10 条 PENDING（或 SENDING 但已过期的），并更新状态为 SENDING 和 claimed_at，然后结束事务，释放锁
            什么时候 SENDING 恢复回 PENDING 状态？解析 json 时出错？发 mq 过程中出现 nAck 或 return 时？

            为什么要有 claim_version？
            因为多实例下，每个实例领取任务后 各自发 mq、mq 回调（publish confirm）中会更新任务状态，
            如果实例 1 领取后但在 mq 执行回调前宕机，然后被实例 2 重新领取，那么实例 1 的任务的 mq 回调可能会操作现在属于实例 2 的任务的状态，把新一轮状态改成 SENT 或 PENDING
            所以要用 claim_version 来避免误改别人的任务状态
        */

        List<OutboxEvent> outboxEvents = outboxEventService.pickJobs();

        for (OutboxEvent event: outboxEvents) {
            switch (event.getEventName()) {
                // 用户注册成功
                case VnotesEvent.USER_REGISTERED -> {
                    // 解析 event json
                    try {
                        UserRegisteredEvent userRegisteredEvent = objectMapper.readValue(event.getEventJson(), UserRegisteredEvent.class);

                        // 发 mq
                        messageProducer.sendUserRegisteredEvent(new UserRegisteredEvent(
                                event.getEventId(),
                                userRegisteredEvent.userId()
                        ), event.getClaimVersion());
                    } catch (JacksonException e) {
                        log.error("解析 event json 失败", e);

                        // 更新状态为 failed
                        // 因为当前是在定时任务环境里执行，所以 markFailed 也要用 claimVersion 来条件更新，避免更新别人的任务状态
                        outboxEventMapper.markFailed(event.getEventId(), event.getClaimVersion());
                    } catch (AmqpException e) {
                        log.error("mq AmqpException", e);

                        // 更新状态为 pending
                        outboxEventMapper.markPending(event.getEventId(), event.getClaimVersion());
                    }
                }

                default -> {

                }
            }
        }
    }
}
