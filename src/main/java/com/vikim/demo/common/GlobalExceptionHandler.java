package com.vikim.demo.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 当 controller @Valid 校验失败时，Controller 抛 MethodArgumentNotValidException 异常，然后流程就会进入 handleValidationException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        Map<String, String> fieldErrors = new HashMap<>();

        // 收集错误 field 和 message 到 fieldErrors
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));


        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Request validation failed",
                request.getRequestURI(),
                fieldErrors
        );

        // 返回 400，响应体为 response
        return ResponseEntity.badRequest().body(response);
    }

    // 如果 Controller 主动抛了 ResponseStatusException，比如 404，就交给这个方法 handleResponseStatusException 处理
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        int status = exception.getStatusCode().value();
        ApiErrorResponse response = new ApiErrorResponse(
                status,
                exception.getReason(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity.status(status).body(response);
    }

    // 并发下，如果数据库侧有问题，会在这里处理
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Data integrity constraint violated",
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
