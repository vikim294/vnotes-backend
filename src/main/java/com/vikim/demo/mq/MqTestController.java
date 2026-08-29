package com.vikim.demo.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mq")
@RequiredArgsConstructor
public class MqTestController {

    private final MessageProducer messageProducer;

    @GetMapping("/test")
    public void send(
    ) {
        messageProducer.sendUserRegisteredEvent(294L);
    }
}