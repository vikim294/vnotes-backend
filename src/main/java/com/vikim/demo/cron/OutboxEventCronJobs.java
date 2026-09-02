package com.vikim.demo.cron;

import com.vikim.demo.common.VnotesEvent;
import com.vikim.demo.mq.MessageProducer;
import com.vikim.demo.mq.events.UserRegisteredEvent;
import com.vikim.demo.outbox.OutboxEvent;
import com.vikim.demo.outbox.OutboxEventMapper;
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
