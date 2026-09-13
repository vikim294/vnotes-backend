package com.vikim.demo.common;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 处理抛出的 业务异常 BusinessException
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException exception
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(),errorCode.getMessage()));
    }



    // 并发下，如果数据库侧有问题，会在这里处理
    // 比如字符串转 boolean -> SQL 数据转换异常
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.DATA_CONFLICT;

        log.error("DataIntegrityViolationException", exception);

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception
    ) {
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleHandlerMethodValidationException(
            HandlerMethodValidationException exception
    ) {
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception
    ) {
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(
                        errorCode.getCode(),
                        "请求体格式错误或包含非法枚举值"
                ));
    }

    // 当 controller @Valid 校验失败时，Controller 抛 MethodArgumentNotValidException 异常，然后流程就会进入 handleValidationException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(errorCode.getMessage());

        // 返回 400，响应体为 response
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(errorCode.getCode(), message));
    }

    // TODO @ExceptionHandler(Exception.class) 可能把 404、405、JSON 无法解析等客户端错误变成 500。
    //  当前学习阶段可以暂时保留，但后面应通过 ResponseEntityExceptionHandler 或专门的异常处理方法补齐这些 MVC 异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
            Exception exception
    ){
        // 未知异常 输出到控制台，并保留完整堆栈
        log.error("Unexpected exception", exception);

        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }
}
