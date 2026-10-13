package com.vikim.vnotes.oauthAccount;

import lombok.Data;

@Data
public class OauthAccount {
    private Long id;
    private Long userId;
    private OauthProvider provider;
    private String providerUserId;
}
