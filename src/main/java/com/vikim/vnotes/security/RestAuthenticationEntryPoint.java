package com.vikim.vnotes.security;

import com.vikim.vnotes.common.ApiResponse;
import com.vikim.vnotes.common.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RestAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /*
     * Spring Security 自带的 Bearer Token 401 处理器。
     * 主要用来保留 WWW-Authenticate 响应头。
     */
    private final AuthenticationEntryPoint delegate =
            new BearerTokenAuthenticationEntryPoint();

    public RestAuthenticationEntryPoint(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        /*
         * 先让 Spring Security 设置：
         *
         * HTTP 状态码
         * WWW-Authenticate: Bearer ...
         */
        delegate.commence(request, response, exception);

        ErrorCode errorCode =
                ErrorCode.AUTHENTICATION_REQUIRED;

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );
        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        ApiResponse<Void> body = ApiResponse.failure(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                body
        );
    }
}