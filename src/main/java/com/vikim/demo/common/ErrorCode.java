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

    DETAIL_NODE_PARENT_VALIDATION_FAILED(
            40003,
            HttpStatus.BAD_REQUEST,
            "详情节点 parent id 校验失败"
    ),

    DETAIL_NODE_NOT_FOUND(
            40004,
            HttpStatus.NOT_FOUND,
            "详情节点不存在"
    ),

    INVALID_DIRECTORY_NODE_RELATION(
            40005,
            HttpStatus.BAD_REQUEST,
            "非法目录节点关系"
    ),

    INVALID_DETAIL_NODE_RELATION(
            40006,
            HttpStatus.BAD_REQUEST,
            "非法详情节点关系"
    ),

    FILE_IS_NOT_PRESENT(
            40007,
            HttpStatus.BAD_REQUEST,
            "未提供文件"
    ),

    USER_NOT_FOUND(
            40008,
            HttpStatus.BAD_REQUEST,
            "用户不存在"
    ),

    UNSUPPORTED_IMAGE_TYPE(
            40009,
            HttpStatus.BAD_REQUEST,
            "图片类型不支持"
    ),

    FILE_CONTENT_TYPE_IS_NULL(
            40010,
            HttpStatus.BAD_REQUEST,
            "文件类型为 null"
    ),

    IMAGE_NODE_FILE_NOT_PROVIDED(
            40011,
            HttpStatus.BAD_REQUEST,
            "缺少对应的图片文件"
    ),

    TEXT_NODE_LABEL_IS_NULL(
            40012,
            HttpStatus.BAD_REQUEST,
            "文本节点 label 不能为空"
    ),

    IMAGE_NODE_FILES_NOT_MATCH(
            40013,
            HttpStatus.BAD_REQUEST,
            "图片节点和文件不匹配"
    ),

    READ_IMPORTED_FILE_FAILED(
            40014,
            HttpStatus.BAD_REQUEST,
            "读取或解析导入文件失败"
    ),

    UNSUPPORTED_VERSION(
            40015,
            HttpStatus.BAD_REQUEST,
            "不支持的版本"
    ),

    TOO_MANY_REQUESTS(
            40016,
            HttpStatus.TOO_MANY_REQUESTS,
            "请求过于频繁"
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



    DIRECTORY_NODE_UPDATED_PARENT_FAILED(
            50004,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "更新目录节点的父节点失败"
    ),

    DIRECTORY_NODE_UPDATED_FAILED(
            50005,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "更新目录节点失败"
    ),

    DIRECTORY_NODE_BATCH_DELETE_FAILED(
            50006,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "批量删除目录节点失败"
    ),

    DETAIL_NODE_CREATED_FAILED(
            50007,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "创建详情节点失败"
    ),



    DETAIL_NODE_UPDATED_PARENT_FAILED(
            50009,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "更新详情节点的父节点失败"
    ),

    DETAIL_NODE_BATCH_DELETE_FAILED(
            50010,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "批量删除详情节点失败"
    ),

    FILE_NAME_IS_NULL(
            50011,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "文件名为 null"
    ),

    FILE_EXTENSION_NOT_FOUNDED(
            50012,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "解析不到文件扩展名"
    ),

    FILE_WRITING_FAILED(
            50013,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "文件写入失败"
    ),

    DIR_CREATED_FAILED(
            50014,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "文件目录创建失败"
    ),


    ;

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
