package com.zixin.healthcenterconsumer.exception;

import com.zixin.utils.exception.BusinessException;
import com.zixin.utils.utils.Result;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

/**
 * 健康报告中心全局异常处理器
 * 
 * 统一处理所有Controller层抛出的异常,返回标准化的错误响应
 * 
 * @author zixin
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     * 
     * BusinessException是业务层主动抛出的可预期异常
     * 例如: 文件大小超限、文件类型不支持、权限不足等
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 - URI: {}, Message: {}, Code: {}", 
                request.getRequestURI(), e.getMessage(), e.getCode());
        return Result.error(e.getMessage());
    }
    
    /**
     * 处理权限不足异常 (403 Forbidden)
     * 
     * 当用户权限不足时,拦截器会抛出ResponseStatusException
     */
    @ExceptionHandler(ResponseStatusException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleResponseStatusException(ResponseStatusException e, HttpServletRequest request) {
        log.warn("权限拒绝 - URI: {}, Reason: {}", 
                request.getRequestURI(), e.getReason());
        return Result.error(e.getReason() != null ? e.getReason() : "Access denied");
    }
    
    /**
     * 处理参数校验异常 (Bean Validation)
     * 
     * 当@Valid或@Validated注解的参数校验失败时抛出
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("参数校验失败 - URI: {}, Errors: {}", request.getRequestURI(), errors);
        return Result.error("参数校验失败: " + errors);
    }
    
    /**
     * 处理绑定异常
     * 
     * 当表单参数绑定失败时抛出
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String errors = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        log.warn("参数绑定失败 - URI: {}, Errors: {}", request.getRequestURI(), errors);
        return Result.error("参数错误: " + errors);
    }
    
    /**
     * 处理文件上传大小超限异常
     * 
     * 当上传文件大小超过配置的限制时抛出
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("文件上传大小超限 - URI: {}, MaxSize: {}", 
                request.getRequestURI(), e.getMaxUploadSize());
        return Result.error("文件大小超过限制,最大允许10MB");
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数 - URI: {}, Message: {}", request.getRequestURI(), e.getMessage());
        return Result.error("参数错误: " + e.getMessage());
    }
    
    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常 - URI: {}", request.getRequestURI(), e);
        return Result.error("系统内部错误,请联系管理员");
    }
    
    /**
     * 处理通用异常
     * 
     * 兜底处理所有未被特定处理器捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("未处理的异常 - URI: {}, Type: {}", 
                request.getRequestURI(), e.getClass().getSimpleName(), e);
        return Result.error("系统异常: " + e.getMessage());
    }
}
