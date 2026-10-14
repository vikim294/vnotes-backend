package com.vikim.vnotes.auth;

import com.vikim.vnotes.common.BusinessException;
import com.vikim.vnotes.common.ErrorCode;
import com.vikim.vnotes.user.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginRateLimitService loginRateLimitService;
    private final LoginVerificationCodeService loginVerificationCodeService;
    private final VerificationCodeSender verificationCodeSender;

    private static final Logger log =
            LoggerFactory.getLogger(AuthService.class);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private Optional<User> findUserByEmail(String email) {
        return authMapper.findUserByEmail(email);
    }

    public Optional<User> findUserById(Long id) {
        return authMapper.findUserById(id);
    }

    public LoginResponse loginByEmailAndPassword(LoginByEmailAndPasswordRequest request) {
        User user = findUserByEmail(request.email()).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 先查 redis 登录限流
        if (loginRateLimitService.hasReachedLimit(user.getId())) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }

        // 校验密码
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            // 更新失败次数
            loginRateLimitService.recordFailure(user.getId());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 生成并返回 token
        return jwtService.generateAccessToken(user.getId(), user.getTokenVersion());
    }

    private String generateCode() {
        int code = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    // 生成验证码 + 发送
    public void sendVerificationCode(SendVerificationCodeRequest request) {
        Optional<User> optionalUser = findUserByEmail(request.email());

        optionalUser.ifPresent(user -> {
            // 使用 SecureRandom 生成六位随机验证码
            String verificationCode = generateCode();

            // 存 redis，60 秒后过期删除
            loginVerificationCodeService.saveVerificationCode(user.getId(), verificationCode);

            // 发送到用户邮箱
            try {
                verificationCodeSender.sendLoginVerificationCode(user.getEmail(), verificationCode);
            } catch (Exception e) {
                // 清空验证码
                loginVerificationCodeService.clearVerificationCode(user.getId());

                // log
                log.error("发送验证码异常", e);

                throw new BusinessException(ErrorCode.EMAIL_SENDING_FAILED);
            }
        });
    }

    public LoginResponse loginByEmailAndVerificationCode(LoginByEmailAndVerificationCodeRequest request) {
        User user = findUserByEmail(request.email()).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_EMAIL_OR_VERIFICATION_CODE));

        // 需要原子地“判断已失败次数、校验验证码、失败时增加失败次数”
        loginVerificationCodeService.verify(user.getId(), request.verificationCode());

        // 生成并返回 token
        return jwtService.generateAccessToken(user.getId(), user.getTokenVersion());
    }
}
