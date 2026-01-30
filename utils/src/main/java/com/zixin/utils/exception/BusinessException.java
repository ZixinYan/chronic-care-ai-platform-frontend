package com.zixin.utils.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 
 * 用于业务层抛出的可预期异常,会被全局异常处理器捕获并返回友好的错误信息
 * 
 * @author zixin
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private int code = 500;
    
    /**
     * 错误消息
     */
    private String message;
    
    /**
     * 构造函数 - 只传入消息
     * 
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.message = message;
    }
    
    /**
     * 构造函数 - 传入消息和原始异常
     * 
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
    
    /**
     * 构造函数 - 传入消息和错误码
     * 
     * @param message 错误消息
     * @param code 错误码
     */
    public BusinessException(String message, int code) {
        super(message);
        this.message = message;
        this.code = code;
    }
    
    /**
     * 构造函数 - 完整参数
     * 
     * @param message 错误消息
     * @param code 错误码
     * @param cause 原始异常
     */
    public BusinessException(String message, int code, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = code;
    }
}
