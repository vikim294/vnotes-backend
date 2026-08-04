package com.vikim.demo.auth;

import com.vikim.demo.user.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

    public LoginResponse generateAccessToken(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt =
                issuedAt.plus(properties.accessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();

        return new LoginResponse(
                token,
                "Bearer",
                properties.accessTokenTtl().toSeconds()
        );
    }
}