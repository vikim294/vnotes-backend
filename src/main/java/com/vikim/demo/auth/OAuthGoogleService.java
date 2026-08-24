package com.vikim.demo.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OAuthGoogleService {
    private final OAuthProperties oAuthProperties;

    public GetGoogleTokenResponse getGoogleTokenByCode(String code){
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("code", code);
        form.add("client_id", oAuthProperties.googleClientId());
        form.add("client_secret", oAuthProperties.googleClientSecret());
        // APPLICATION_FORM_URLENCODED 无需自己 encode
        form.add("redirect_uri", oAuthProperties.googleRedirectUri());
        form.add("grant_type", "authorization_code");

        RestClient restClient = RestClient.create();
        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(
                        MediaType.APPLICATION_FORM_URLENCODED
                )
                .body(form)
                .retrieve()
                .body(GetGoogleTokenResponse.class);
    }

    public GoogleUserInfo verifyAndParseIdToken(String idToken) {
        GoogleIdTokenVerifier verifier =
                new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance()
                )
                        .setAudience(List.of(oAuthProperties.googleClientId()))
                        .build();

        try {
            GoogleIdToken res = verifier.verify(idToken);

            if (res == null) {
                throw new RuntimeException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = res.getPayload();

            return new GoogleUserInfo(
                    payload.getSubject(),
                    payload.getEmail(),
                    Boolean.TRUE.equals(payload.getEmailVerified()),
                    (String) payload.get("name")
            );

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to verify Google ID token", e);
        }
    }
}
