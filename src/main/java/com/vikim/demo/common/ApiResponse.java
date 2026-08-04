package com.vikim.demo.common;

// 统一响应结构（正常响应、异常响应）
public record ApiResponse<T>(
        int code,
        T data,
        String message
) {
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(0, data, message);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(0, null, message);
    }

    public static <T> ApiResponse<T> failure(int code, String message) {
        return new ApiResponse<>(code, null, message);
    }
}
