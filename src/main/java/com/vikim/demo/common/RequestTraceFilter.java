package com.vikim.demo.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTraceFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(RequestTraceFilter.class);

    // 防止客户端把换行符等不可信内容放进日志；也兼容 UUID 和常见网关生成的 ID。
    private static final Pattern SAFE_ID =
            Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,63}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Map<String, String> previousContext = MDC.getCopyOfContextMap();

        // 读取请求头 X-Request-Id、X-Trace-Id
        // 没有提供或格式不安全时生成 UUID
        String requestId = useOrGenerateId(
                request.getHeader(TraceContext.REQUEST_ID_HEADER)
        );
        String traceId = useOrGenerateId(
                request.getHeader(TraceContext.TRACE_ID_HEADER)
        );

        // 放入 MDC 上下文
        MDC.put(TraceContext.REQUEST_ID, requestId);
        MDC.put(TraceContext.TRACE_ID, traceId);

        // 放入响应头，方便前端/用户获取
        response.setHeader(TraceContext.REQUEST_ID_HEADER, requestId);
        response.setHeader(TraceContext.TRACE_ID_HEADER, traceId);

        long startedAt = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;

            // 记录 method、URI、状态码、耗时
            log.info(
                    "HTTP {} {} -> {}, durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs
            );

            // 最终清理、恢复 MDC，防止线程复用导致串号
            restoreMdc(previousContext);
        }
    }

    private String useOrGenerateId(String candidate) {
        if (candidate != null && SAFE_ID.matcher(candidate).matches()) {
            return candidate;
        }

        return UUID.randomUUID().toString();
    }

    private void restoreMdc(Map<String, String> previousContext) {
        MDC.clear();

        if (previousContext != null) {
            MDC.setContextMap(previousContext);
        }
    }
}
