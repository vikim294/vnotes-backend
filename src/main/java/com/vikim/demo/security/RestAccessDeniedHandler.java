package com.vikim.demo.security;

import com.vikim.demo.common.ApiResponse;
import com.vikim.demo.common.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RestAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /*
     * 保留 Bearer Token 的标准响应头。
     */
    private final AccessDeniedHandler delegate =
            new BearerTokenAccessDeniedHandler();

    public RestAccessDeniedHandler(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException, ServletException {
        /*
         * 设置 403 和 WWW-Authenticate 响应头。
         */
        delegate.handle(request, response, exception);

        ErrorCode errorCode =
                ErrorCode.ACCESS_DENIED;

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