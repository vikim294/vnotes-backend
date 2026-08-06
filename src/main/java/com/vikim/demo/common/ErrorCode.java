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

    INVALID_CREDENTIALS(
            30001,
            HttpStatus.UNAUTHORIZED,
            "邮箱或密码错误"
    ),

    AUTHENTICATION_REQUIRED(
            30002,
            HttpStatus.UNAUTHORIZED,
            "未登录或访问令牌无效"
    ),

    ACCESS_DENIED(
            30003,
            HttpStatus.FORBIDDEN,
            "无权访问该资源"
    ),

    DIRECTORY_NODE_NOT_FOUND(
            40001,
            HttpStatus.NOT_FOUND,
            "目录节点不存在"
    ),

    DIRECTORY_NODE_PARENT_VALIDATION_FAILED(
            40002,
            HttpStatus.BAD_REQUEST,
            "目录节点 parent id 校验失败"
    ),

    INTERNAL_ERROR(
            50001,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "服务器内部错误"
    ),

    DIRECTORY_NODE_CREATED_FAILED(
            50002,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "创建目录节点失败"
    ),

    INVALID_DIRECTORY_NODE_RELATION(
            50003,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "非法目录节点关系"
    ),

    DIRECTORY_NODE_UPDATED_PARENT_FAILED(
            50004,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "更新目录节点的父节点失败"
    ),

    DIRECTORY_NODE_UPDATED_FAILED(
            50005,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "更新目录节点失败"
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
