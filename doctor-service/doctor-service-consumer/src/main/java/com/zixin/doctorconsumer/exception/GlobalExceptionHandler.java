package com.zixin.doctorconsumer.exception;

import com.zixin.utils.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * 全局异常处理器
 * 处理权限校验失败等异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理权限不足异常 (403 Forbidden)
     */
    @ExceptionHandler(ResponseStatusException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleResponseStatusException(ResponseStatusException e) {
        log.warn("Access forbidden: {}", e.getReason());
        return Result.error(e.getReason() != null ? e.getReason() : "Access denied");
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception e) {
        log.error("Unexpected error occurred", e);
        return Result.error("Internal server error: " + e.getMessage());
    }
}
