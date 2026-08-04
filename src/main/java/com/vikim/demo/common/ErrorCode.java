package com.vikim.demo.common;

import org.springframework.http.HttpStatus;

/*
    集中管理业务码、HTTP 状态码、响应消息
 */
public enum ErrorCode {
    VALIDATION_FAILED(
            10001,
            HttpStatus.BAD_REQUEST,
            "请求参数校验失败"
    ),
    EMAIL_ALREADY_EXISTS(
            20001,
            HttpStatus.CONFLICT,
            "用户邮箱已存在"
    ),

    DATA_CONFLICT(
            20002,
            HttpStatus.CONFLICT,
            "数据冲突"
    ),

    INTERNAL_ERROR(
            50001,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "服务器内部错误"
    );

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
