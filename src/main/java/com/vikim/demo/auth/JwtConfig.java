package com.vikim.demo.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public JwtEncoder jwtEncoder(JwtProperties properties) {
        byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder()
                    .decode(properties.secret());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "JWT_SECRET 必须是合法的 Base64 字符串",
                    exception
            );
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET 解码后不能少于 32 字节"
            );
        }

        SecretKey secretKey =
                new SecretKeySpec(keyBytes, "HmacSHA256");

        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }
}