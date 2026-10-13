package com.vikim.vnotes.auth;

public interface VerificationCodeSender {
    void sendLoginVerificationCode(String email, String verificationCode) throws Exception;
}
