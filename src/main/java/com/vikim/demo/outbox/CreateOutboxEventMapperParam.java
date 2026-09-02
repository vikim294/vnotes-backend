package com.vikim.demo.outbox;

import com.vikim.demo.common.VnotesEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateOutboxEventMapperParam {
    private String eventId;

    private VnotesEvent eventName;

    private String eventJson;

    private OutboxEventStatus status;
}
