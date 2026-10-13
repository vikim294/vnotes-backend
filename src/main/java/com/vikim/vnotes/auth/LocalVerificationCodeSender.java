package com.vikim.vnotes.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class LocalVerificationCodeSender implements VerificationCodeSender {

    private static final Logger log =
            LoggerFactory.getLogger(LocalVerificationCodeSender.class);

    @Override
    public void sendLoginVerificationCode(String email, String verificationCode) {
        log.info("开发环境邮件验证码：email={}, verificationCode={}", email, verificationCode);
    }
}
