package com.vikim.demo.common;

import java.util.Map;

public record ApiErrorResponse(
        int code,
        String msg,
        String url,
        Map<String, String> fieldErrors
) {
}
