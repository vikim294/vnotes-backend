package com.vikim.demo.auth;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public JwtService(
            JwtEncoder jwtEncoder,
            JwtProperties properties
    ) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public LoginResponse generateAccessToken(Long userId, Long tokenVersion) {
        Instant issuedAt = Instant.now();
        Instant expiresAt =
                issuedAt.plus(properties.accessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(userId.toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                // 塞自定义数据
                .claim("tokenType", "CLIENT")
                .claim("tokenVersion", tokenVersion)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();

        return new LoginResponse(
                token,
                properties.accessTokenTtl().toSeconds(),
                userId
        );
    }

    public LoginResponse generateAccessToken(Long userId, List<String> roleCodes, List<String> permissionCodes) {
        Instant issuedAt = Instant.now();
        Instant expiresAt =
                issuedAt.plus(properties.accessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(userId.toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                // 塞自定义数据
                .claim("tokenType", "ADMIN")
                .claim("roles", roleCodes)
                .claim("permissions", permissionCodes)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();

        return new LoginResponse(
                token,
                properties.accessTokenTtl().toSeconds(),
                userId
        );
    }
}