package com.vikim.vnotes.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterAccountMapperParam {
    private Long id;
    private String username;
    private String passwordHash;
    private String email;
}
