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

    INVALID_EMAIL_OR_VERIFICATION_CODE(
            40017,
            HttpStatus.UNAUTHORIZED,
            "无效的邮箱或验证码"
    ),

    OLD_AND_DEST_DIRECTORY_NODES_ARE_SAME_CODE(
            40018,
            HttpStatus.BAD_REQUEST,
            "新目录节点不能是旧目录节点"
    ),

    OAUTH_LOGIN_CODE_INVALID_OR_EXPIRED(
            40019,
            HttpStatus.BAD_REQUEST,
            "无效的 code"
    ),

    INVALID_TAGS(
            40020,
            HttpStatus.BAD_REQUEST,
            "无效的 tags"
    ),

    NOTE_SHARE_NOT_FOUND(
            40022,
            HttpStatus.NOT_FOUND,
            "分享未找到"
    ),

    NOTE_HAS_ALREADY_REVOKED(
            40023,
            HttpStatus.BAD_REQUEST,
            "无法取消分享未被分享的笔记"
    ),

    ARTICLE_NOT_FOUND(
            40024,
            HttpStatus.BAD_REQUEST,
            "文章未找到"
    ),

    INVALID_PASSWORD_INPUT(
            40025,
            HttpStatus.BAD_REQUEST,
            "新旧密码请求参数不能一样"
    ),

    INCORRECT_OLD_PASSWORD(
            40026,
            HttpStatus.BAD_REQUEST,
            "旧密码输入错误"
    ),

    UNSUPPORTED_FOR_OAUTH_ACCOUNT(
            40027,
            HttpStatus.BAD_REQUEST,
            "OAuth 账号不支持该操作"
    ),

    INVALID_ARTICLE_RELATION(
            40028,
            HttpStatus.BAD_REQUEST,
            "错误的详情节点-文章关系"
    ),

    INVALID_REF_ON_TEXT_NODE(
            40029,
            HttpStatus.BAD_REQUEST,
            "文本节点不能有引用"
    ),

    INVALID_REF_ON_IMAGE_NODE(
            40030,
            HttpStatus.BAD_REQUEST,
            "图片节点不能有引用"
    ),

    NODE_REF_MUST_BE_ONLY_ONE(
            40031,
            HttpStatus.BAD_REQUEST,
            "引用节点的引用必须唯一"
    ),

    REF_NODE_MISSING_VALUE(
            40032,
            HttpStatus.BAD_REQUEST,
            "引用节点的引用缺失"
    ),

    REF_NODE_SHOULD_NOT_HAVE_ARTICLE(
            40033,
            HttpStatus.BAD_REQUEST,
            "引用节点不能有文章"
    ),

    REF_NODE_VALIDATED_FAILED(
            40034,
            HttpStatus.BAD_REQUEST,
            "引用节点校验失败"
    ),

    REF_NODE_LABEL_IS_NULL(
            40035,
            HttpStatus.BAD_REQUEST,
            "引用节点 label 不能为空"
    ),

    NODE_REF_IS_TO_BE_DELETED(
            40036,
            HttpStatus.BAD_REQUEST,
            "引用了将要被删除的详情节点"
    ),

    CONTENT_TOO_LARGE(
            40037,
            HttpStatus.CONTENT_TOO_LARGE,
            "文件大小超过限制"
    ),

    FILE_TOO_LARGE(
            40038,
            HttpStatus.CONTENT_TOO_LARGE,
            "文件大小超过限制"
    ),

    FILES_TOTAL_SIZE_EXCEEDED(
            40039,
            HttpStatus.CONTENT_TOO_LARGE,
            "文件总大小超过限制"
    ),

    INVALID_IMAGE(
            40040,
            HttpStatus.BAD_REQUEST,
            "非法图片"
    ),

    IMAGE_DIMENSIONS_EXCEEDED(
            40041,
            HttpStatus.BAD_REQUEST,
            "图片宽高超过限制"
    ),

    IMAGE_PIXELS_EXCEEDED(
            40042,
            HttpStatus.BAD_REQUEST,
            "图片像素超过限制"
    ),

    ADMIN_ROLE_NOT_FOUND(
            40043,
            HttpStatus.NOT_FOUND,
            "admin/角色未找到"
    ),

    ADMIN_USER_NOT_FOUND(
            40044,
            HttpStatus.NOT_FOUND,
            "admin/用户未找到"
    ),

    ADMIN_INVALID_EMAIL_OR_PASSWORD(
            40045,
            HttpStatus.BAD_REQUEST,
            "admin/非法邮箱或密码"
    ),

    ADMIN_OPERATION_DENIED(
            40046,
            HttpStatus.FORBIDDEN,
            "admin/操作拒绝"
    ),

    ADMIN_ROLE_NAME_ALREADY_EXISTS(
            40047,
            HttpStatus.CONFLICT,
            "admin/角色名已存在"
    ),

    ADMIN_USER_ALREADY_EXISTS(
            40048,
            HttpStatus.CONFLICT,
            "admin/用户已存在"
    ),

    USER_IS_DISABLED(
            40049,
            HttpStatus.FORBIDDEN,
            "用户已被封禁"
    ),

    INVALID_CURSOR(
            40050,
            HttpStatus.BAD_REQUEST,
            "非法 cursor 参数"
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

    DIRECTORY_NODE_BATCH_RESTORE_FAILED(
            50008,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "批量恢复目录节点失败"
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

    EMAIL_SENDING_FAILED(
            50015,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "邮件发送失败"
    ),

    DIRECTORY_NODE_UPDATED_ORIGINAL_PARENT_ID_FAILED(
            50016,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "目录节点更新 original parent id 失败"
    ),

    DIRECTORY_NODE_UPDATED_DELETED_AT_AND_DELETED_ROOT_ID_FAILED(
            50017,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "目录节点更新 deleted at 或 deleted root id 失败"
    ),

    DIRECTORY_NODE_RESTORE_PARENT_FAILED(
            50018,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "目录节点恢复 parent id失败"
    ),

    DIRECTORY_NODE_HARD_DELETE_FAILED(
            50019,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "硬删除目录节点失败"
    ),

    DETAIL_NODE_MOVE_FAILED(
            50020,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "移动详情节点失败"
    ),

    USER_CREATED_FAILED(
            50021,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "用户创建失败"
    ),

    OAUTH_ACCOUNT_CREATED_FAILED(
            50022,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "OAuth account 创建失败"
    ),

    UPLOAD_OBJECT_STORAGE_FAILED(
            50023,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "上传对象存储失败"
    ),

    CREATE_TAG_FAILED(
            50024,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "创建标签失败"
    ),

    UPDATE_TAG_FAILED(
            50025,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "更新标签失败"
    ),

    DELETE_TAG_FAILED(
            50026,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "删除标签失败"
    ),

    UPDATE_NOTE_SHARE_FAILED(
            50027,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "更新笔记分享失败"
    ),

    CREATE_NOTE_SHARE_FAILED(
            50028,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "新增笔记分享失败"
    ),

    CREATE_ARTICLE_FAILED(
            50029,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "新增文章失败"
    ),

    CHANGE_PASSWORD_FAILED(
            50030,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "修改密码失败"
    ),

    ADMIN_USER_CREATED_FAILED(
            50031,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "admin/用户创建失败"
    ),

    ADMIN_ASSIGN_ROLE_FAILED(
            50032,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "admin/指定角色失败"
    ),

    ADMIN_CREATE_AUDIT_LOG_FAILED(
            50033,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "admin/创建审计日志失败"
    ),

    ADMIN_ASSIGN_PERMISSION_FAILED(
            50034,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "admin/指定权限失败"
    ),

    ADMIN_CREATE_ROLE_FAILED(
            50035,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "admin/创建角色失败"
    ),

    ADMIN_UPDATE_ROLE_FAILED(
            50036,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "admin/更新角色失败"
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
