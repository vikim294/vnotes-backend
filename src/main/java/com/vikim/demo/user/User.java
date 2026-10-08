package com.vikim.demo.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class User {
    private Long id;

    private String username;

    private String email;

    private String passwordHash;

    private String avatarUrl;

    private LocalDateTime createdAt;

    private UserStatus status;

    private Long tokenVersion;
}