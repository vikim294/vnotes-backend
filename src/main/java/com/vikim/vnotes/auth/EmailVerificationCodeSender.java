package com.vikim.vnotes.auth;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
@RequiredArgsConstructor
public class EmailVerificationCodeSender implements VerificationCodeSender {

    private final EmailSenderProperties emailSenderProperties;

    @Override
    public void sendLoginVerificationCode(String email, String verificationCode) throws ResendException {
        Resend resend = new Resend(emailSenderProperties.apiKey());
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("no-reply@noti.vnote.cloud")
                .to(email)
                .subject("Your Vnotes Verification Code")
                .html("code: " + verificationCode)
                .build();

        resend.emails().send(params);
    }
}
