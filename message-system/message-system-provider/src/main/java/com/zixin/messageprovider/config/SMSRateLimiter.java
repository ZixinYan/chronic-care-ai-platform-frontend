package com.zixin.messageprovider.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * SMS防刷限流器
 * 
 * 基于Redis实现的滑动窗口防刷机制：
 * 1. 同一手机号在指定时间窗口内只能发送N次短信
 * 2. 同一发送者在指定时间窗口内只能发送N次短信  
 * 3. 使用Redis的INCR和EXPIRE命令实现原子性操作
 * 
 * 防刷策略：
 * - 60秒内同一手机号最多发送1条短信
 * - 60秒内同一发送者最多发送5条短信
 * - 24小时内同一手机号最多发送10条短信
 * 
 * @author zixin
 */
@Slf4j
@Component
public class SMSRateLimiter {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * Redis Key前缀
     */
    private static final String PHONE_RATE_LIMIT_KEY_PREFIX = "sms:rate_limit:phone:";
    private static final String SENDER_RATE_LIMIT_KEY_PREFIX = "sms:rate_limit:sender:";
    private static final String PHONE_DAILY_LIMIT_KEY_PREFIX = "sms:daily_limit:phone:";
    
    /**
     * 限流配置
     */
    // 短期限流（防止频繁发送）
    private static final int PHONE_SHORT_LIMIT = 1;          // 60秒内同一手机号最多1条
    private static final int PHONE_SHORT_WINDOW_SECONDS = 60; // 60秒窗口
    
    private static final int SENDER_SHORT_LIMIT = 5;          // 60秒内同一发送者最多5条
    private static final int SENDER_SHORT_WINDOW_SECONDS = 60;// 60秒窗口
    
    // 长期限流（防止恶意刷短信）
    private static final int PHONE_DAILY_LIMIT = 10;          // 24小时内同一手机号最多10条
    private static final int PHONE_DAILY_WINDOW_SECONDS = 86400; // 24小时窗口
    
    /**
     * 检查是否允许发送短信
     * 
     * @param phone 接收者手机号
     * @param senderId 发送者ID
     * @return RateLimitResult 限流检查结果
     */
    public RateLimitResult checkRateLimit(String phone, Long senderId) {
        try {
            // 1. 检查手机号短期限流（60秒内最多1条）
            RateLimitResult phoneShortResult = checkLimit(
                PHONE_RATE_LIMIT_KEY_PREFIX + phone,
                PHONE_SHORT_LIMIT,
                PHONE_SHORT_WINDOW_SECONDS,
                "手机号"
            );
            if (!phoneShortResult.isAllowed()) {
                return phoneShortResult;
            }
            
            // 2. 检查发送者短期限流（60秒内最多5条）
            RateLimitResult senderShortResult = checkLimit(
                SENDER_RATE_LIMIT_KEY_PREFIX + senderId,
                SENDER_SHORT_LIMIT,
                SENDER_SHORT_WINDOW_SECONDS,
                "发送者"
            );
            if (!senderShortResult.isAllowed()) {
                return senderShortResult;
            }
            
            // 3. 检查手机号每日限流（24小时内最多10条）
            RateLimitResult phoneDailyResult = checkLimit(
                PHONE_DAILY_LIMIT_KEY_PREFIX + phone,
                PHONE_DAILY_LIMIT,
                PHONE_DAILY_WINDOW_SECONDS,
                "手机号每日"
            );
            if (!phoneDailyResult.isAllowed()) {
                return phoneDailyResult;
            }
            
            // 全部通过
            return RateLimitResult.allowed();
            
        } catch (Exception e) {
            log.error("Rate limit check error, phone: {}, senderId: {}", phone, senderId, e);
            // 如果Redis异常，为了不影响业务，降级为允许通过（可根据实际需求调整）
            return RateLimitResult.allowed();
        }
    }
    
    /**
     * 记录实际发送（增加计数器）
     * 
     * 只有在短信真正发送成功后才调用此方法
     * 
     * @param phone 接收者手机号
     * @param senderId 发送者ID
     */
    public void recordSending(String phone, Long senderId) {
        try {
            // 增加手机号短期计数器
            String phoneShortKey = PHONE_RATE_LIMIT_KEY_PREFIX + phone;
            incrementCounter(phoneShortKey, PHONE_SHORT_WINDOW_SECONDS);
            
            // 增加发送者短期计数器
            String senderShortKey = SENDER_RATE_LIMIT_KEY_PREFIX + senderId;
            incrementCounter(senderShortKey, SENDER_SHORT_WINDOW_SECONDS);
            
            // 增加手机号每日计数器
            String phoneDailyKey = PHONE_DAILY_LIMIT_KEY_PREFIX + phone;
            incrementCounter(phoneDailyKey, PHONE_DAILY_WINDOW_SECONDS);
            
            log.debug("Recorded SMS sending, phone: {}, senderId: {}", phone, senderId);
            
        } catch (Exception e) {
            log.error("Failed to record SMS sending, phone: {}, senderId: {}", phone, senderId, e);
            // 记录失败不影响业务，只记录日志
        }
    }
    
    /**
     * 检查单个限流条件
     * 
     * @param key Redis键
     * @param limit 限制次数
     * @param windowSeconds 时间窗口（秒）
     * @param limitType 限流类型（用于日志）
     * @return RateLimitResult 检查结果
     */
    private RateLimitResult checkLimit(String key, int limit, int windowSeconds, String limitType) {
        try {
            // 获取当前计数
            String countStr = redisTemplate.opsForValue().get(key);
            int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
            
            if (currentCount >= limit) {
                log.warn("Rate limit exceeded, type: {}, key: {}, current: {}, limit: {}", 
                        limitType, key, currentCount, limit);
                
                // 获取剩余过期时间
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                int remainingSeconds = ttl != null && ttl > 0 ? ttl.intValue() : windowSeconds;
                
                return RateLimitResult.rejected(
                    String.format("%s发送过于频繁，请在%d秒后重试", limitType, remainingSeconds),
                    remainingSeconds
                );
            }
            
            return RateLimitResult.allowed();
            
        } catch (Exception e) {
            log.error("Check limit error, key: {}", key, e);
            // Redis异常时降级为允许
            return RateLimitResult.allowed();
        }
    }
    
    /**
     * 增加计数器
     * 
     * @param key Redis键
     * @param expireSeconds 过期时间（秒）
     */
    private void incrementCounter(String key, int expireSeconds) {
        try {
            // 使用INCR原子性增加计数
            Long newCount = redisTemplate.opsForValue().increment(key);
            
            // 如果是第一次计数（newCount == 1），设置过期时间
            if (newCount != null && newCount == 1) {
                redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
            }
            
        } catch (Exception e) {
            log.error("Increment counter error, key: {}", key, e);
        }
    }
    
    /**
     * 限流检查结果
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final String message;
        private final int retryAfterSeconds;
        
        private RateLimitResult(boolean allowed, String message, int retryAfterSeconds) {
            this.allowed = allowed;
            this.message = message;
            this.retryAfterSeconds = retryAfterSeconds;
        }
        
        public static RateLimitResult allowed() {
            return new RateLimitResult(true, null, 0);
        }
        
        public static RateLimitResult rejected(String message, int retryAfterSeconds) {
            return new RateLimitResult(false, message, retryAfterSeconds);
        }
        
        public boolean isAllowed() {
            return allowed;
        }
        
        public String getMessage() {
            return message;
        }
        
        public int getRetryAfterSeconds() {
            return retryAfterSeconds;
        }
    }
}
