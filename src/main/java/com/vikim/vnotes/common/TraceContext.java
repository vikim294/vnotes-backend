package com.vikim.vnotes.common;

public final class TraceContext {

    public static final String REQUEST_ID = "requestId";
    public static final String TRACE_ID = "traceId";

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private TraceContext() {
    }
}
