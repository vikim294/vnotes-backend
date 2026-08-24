package com.vikim.demo.oauthAccount;

import lombok.Data;

@Data
public class OauthAccount {
    private Long id;
    private Long userId;
    private OauthProvider provider;
    private String providerUserId;
}
