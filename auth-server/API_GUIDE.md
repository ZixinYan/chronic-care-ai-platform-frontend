# API使用指南

## 目录
- [快速开始](#快速开始)
- [API接口说明](#api接口说明)
- [使用示例](#使用示例)
- [错误处理](#错误处理)
- [最佳实践](#最佳实践)

## 快速开始

### 1. 添加依赖

在你的服务中添加auth-api依赖:

```xml
<dependency>
    <groupId>com.zixin</groupId>
    <artifactId>auth-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 引用服务

使用Dubbo引用认证服务:

```java
@DubboReference
private JwtAPI jwtAPI;

@DubboReference
private TokenValidationAPI tokenValidationAPI;
```

### 3. 配置Redis

确保你的application.properties中配置了Redis:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

## API接口说明

### JwtAPI

#### 1. genToken - 生成Token对

生成Access Token和Refresh Token。

**请求参数**:
```java
GenTokenRequest {
    Long userId;           // 用户ID (必填)
    String username;       // 用户名 (可选)
    List<String> roles;    // 角色列表 (可选)
}
```

**响应参数**:
```java
GenTokenResponse {
    String code;           // 响应码
    String message;        // 响应消息
    String accessToken;    // 访问令牌
    String refreshToken;   // 刷新令牌
    Long expiresIn;       // 过期时间(秒)
    String tokenType;      // 令牌类型(Bearer)
}
```

#### 2. refreshToken - 刷新Token

使用Refresh Token获取新的Token对。

**请求参数**:
```java
RefreshTokenRequest {
    String refreshToken;   // Refresh Token (必填)
}
```

**响应参数**:
```java
RefreshTokenResponse {
    String code;           // 响应码
    String message;        // 响应消息
    String accessToken;    // 新的访问令牌
    String refreshToken;   // 新的刷新令牌
    Long expiresIn;       // 过期时间(秒)
    String tokenType;      // 令牌类型(Bearer)
}
```

### TokenValidationAPI

#### 1. validateToken - 验证Token

验证Token是否有效，并返回用户信息。

**请求参数**:
```java
ValidateTokenRequest {
    String token;          // JWT Token (必填)
}
```

**响应参数**:
```java
ValidateTokenResponse {
    String code;           // 响应码
    String message;        // 响应消息
    Boolean valid;         // Token是否有效
    Long userId;           // 用户ID
    String username;       // 用户名
    List<String> roles;    // 角色列表
    List<String> authorities; // 权限列表
}
```

#### 2. revokeToken - 撤销Token

将Token加入黑名单，使其失效(登出)。

**请求参数**:
```java
ValidateTokenRequest {
    String token;          // JWT Token (必填)
}
```

**响应参数**:
```java
ValidateTokenResponse {
    String code;           // 响应码
    String message;        // 响应消息
}
```

## 使用示例

### 场景1: 用户登录

```java
@Service
public class LoginService {
    
    @DubboReference
    private JwtAPI jwtAPI;
    
    public LoginResponse login(String username, String password) {
        // 1. 验证用户名和密码
        User user = userService.validateCredentials(username, password);
        if (user == null) {
            throw new AuthenticationException("Invalid credentials");
        }
        
        // 2. 获取用户角色
        List<String> roles = userService.getUserRoles(user.getId());
        
        // 3. 生成Token对
        GenTokenRequest request = new GenTokenRequest();
        request.setUserId(user.getId());
        request.setUsername(user.getUsername());
        request.setRoles(roles);
        
        GenTokenResponse response = jwtAPI.genToken(request);
        
        // 4. 检查结果
        if (!response.getCode().isSuccess()) {
            throw new RuntimeException("Token generation failed");
        }
        
        // 5. 返回Token
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(response.getAccessToken());
        loginResponse.setRefreshToken(response.getRefreshToken());
        loginResponse.setExpiresIn(response.getExpiresIn());
        
        return loginResponse;
    }
}
```

### 场景2: Token刷新

```java
@Service
public class TokenService {
    
    @DubboReference
    private JwtAPI jwtAPI;
    
    public TokenRefreshResult refreshToken(String refreshToken) {
        // 1. 构造刷新请求
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);
        
        // 2. 调用刷新接口
        RefreshTokenResponse response = jwtAPI.refreshToken(request);
        
        // 3. 检查结果
        if (!response.getCode().isSuccess()) {
            throw new TokenRefreshException("Token refresh failed: " + response.getMessage());
        }
        
        // 4. 返回新Token
        TokenRefreshResult result = new TokenRefreshResult();
        result.setAccessToken(response.getAccessToken());
        result.setRefreshToken(response.getRefreshToken());
        result.setExpiresIn(response.getExpiresIn());
        
        return result;
    }
}
```

### 场景3: API网关Token验证

```java
@Component
public class GatewayAuthFilter implements GlobalFilter, Ordered {
    
    @DubboReference
    private TokenValidationAPI tokenValidationAPI;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 从请求头获取Token
        String token = extractToken(exchange.getRequest());
        
        if (token == null) {
            return unauthorized(exchange);
        }
        
        // 2. 验证Token
        ValidateTokenRequest request = new ValidateTokenRequest();
        request.setToken(token);
        
        ValidateTokenResponse response = tokenValidationAPI.validateToken(request);
        
        // 3. 检查验证结果
        if (!response.getValid()) {
            return unauthorized(exchange);
        }
        
        // 4. 将用户信息添加到请求头
        ServerHttpRequest modifiedRequest = exchange.getRequest()
                .mutate()
                .header("X-User-Id", response.getUserId().toString())
                .header("X-User-Roles", String.join(",", response.getRoles()))
                .build();
        
        // 5. 继续过滤器链
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
    
    @Override
    public int getOrder() {
        return -100; // 高优先级
    }
}
```

### 场景4: 用户登出

```java
@Service
public class LogoutService {
    
    @DubboReference
    private TokenValidationAPI tokenValidationAPI;
    
    public void logout(String accessToken) {
        // 1. 构造撤销请求
        ValidateTokenRequest request = new ValidateTokenRequest();
        request.setToken(accessToken);
        
        // 2. 撤销Token
        ValidateTokenResponse response = tokenValidationAPI.revokeToken(request);
        
        // 3. 检查结果
        if (!response.getCode().isSuccess()) {
            throw new RuntimeException("Logout failed: " + response.getMessage());
        }
        
        log.info("User logged out successfully");
    }
}
```

## 错误处理

### 常见错误码

| 错误码 | 说明 | 处理方式 |
|-------|------|---------|
| SUCCESS | 成功 | 正常处理 |
| FAIL | 失败 | 根据message字段判断具体原因 |

### 常见错误场景

#### 1. Token过期

**错误信息**: "Invalid or expired token"

**处理方式**:
```java
try {
    ValidateTokenResponse response = tokenValidationAPI.validateToken(request);
    if (!response.getValid()) {
        // Token无效或过期，尝试刷新
        return refreshTokenAndRetry();
    }
} catch (Exception e) {
    // Token完全无效，需要重新登录
    redirectToLogin();
}
```

#### 2. Refresh Token无效

**错误信息**: "Invalid or expired refresh token"

**处理方式**:
```java
RefreshTokenResponse response = jwtAPI.refreshToken(request);
if (!response.getCode().isSuccess()) {
    // Refresh Token无效，需要重新登录
    redirectToLogin();
}
```

#### 3. Token已被撤销

**错误信息**: "Token is blacklisted"

**处理方式**:
```java
// Token已被撤销(用户已登出)，需要重新登录
redirectToLogin();
```

## 最佳实践

### 1. Token存储

**前端存储建议**:
- Access Token: 存储在内存中(不要存localStorage)
- Refresh Token: 存储在HttpOnly Cookie中(推荐)或安全的存储中

```javascript
// 不推荐
localStorage.setItem('accessToken', token);

// 推荐
// 将Refresh Token存储在HttpOnly Cookie中
// Access Token存储在内存变量中
```

### 2. Token刷新策略

**主动刷新**:
```java
// 在Token过期前5分钟主动刷新
if (tokenExpiresIn < 300) { // 5分钟
    refreshToken();
}
```

**被动刷新**:
```java
// 收到401错误后尝试刷新
if (response.getStatusCode() == 401) {
    String newToken = refreshToken();
    retryWithNewToken(newToken);
}
```

### 3. 并发刷新控制

```java
@Service
public class TokenRefreshService {
    
    private final Lock refreshLock = new ReentrantLock();
    private volatile String cachedAccessToken;
    
    public String getValidAccessToken(String refreshToken) {
        // 避免并发刷新
        refreshLock.lock();
        try {
            if (isTokenValid(cachedAccessToken)) {
                return cachedAccessToken;
            }
            
            // 刷新Token
            RefreshTokenResponse response = refreshToken(refreshToken);
            cachedAccessToken = response.getAccessToken();
            return cachedAccessToken;
        } finally {
            refreshLock.unlock();
        }
    }
}
```

### 4. 权限检查

**方式1: 使用注解**
```java
@RequireRole("ADMIN")
public void adminMethod() {
    // ...
}

@RequirePermission("user:write")
public void updateUser() {
    // ...
}
```

**方式2: 手动检查**
```java
public void someMethod() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (!hasRole(auth, "ADMIN")) {
        throw new AccessDeniedException("Requires ADMIN role");
    }
    // ...
}
```

### 5. 生产环境配置

```properties
# 缩短Access Token有效期
jwt.access-token.expiration=900  # 15分钟

# Refresh Token有效期
jwt.refresh-token.expiration=86400  # 1天

# 增大RSA密钥大小
jwt.key-size=4096

# 启用HTTPS
server.ssl.enabled=true
```

### 6. 监控和日志

```java
@Aspect
@Component
public class TokenMonitoringAspect {
    
    @Around("@annotation(com.zixin.authprovider.annotation.RequireRole)")
    public Object monitorRoleCheck(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            log.info("Role check passed, duration: {}ms", 
                    System.currentTimeMillis() - startTime);
            return result;
        } catch (AccessDeniedException e) {
            log.warn("Role check failed: {}", e.getMessage());
            throw e;
        }
    }
}
```

## 安全建议

1. ✅ 始终使用HTTPS传输Token
2. ✅ 不要在URL中传递Token
3. ✅ 定期轮换密钥
4. ✅ 监控异常登录行为
5. ✅ 实施访问频率限制
6. ✅ 记录所有认证和授权操作
7. ✅ 定期审查权限配置
8. ✅ 使用强密码策略

## 常见问题

### Q: Access Token和Refresh Token应该如何存储?

A: 
- **Access Token**: 存储在内存中，不要存储在localStorage(防止XSS攻击)
- **Refresh Token**: 存储在HttpOnly Cookie中(防止JavaScript访问)

### Q: Token刷新失败应该如何处理?

A: 如果Refresh Token刷新失败，说明用户需要重新登录。引导用户到登录页面。

### Q: 如何实现"记住我"功能?

A: 延长Refresh Token的有效期，比如设置为30天。

### Q: 如何踢出特定用户?

A: 调用`revokeToken`接口撤销用户的所有Token，或者在Redis中删除用户的所有Refresh Token。

### Q: 如何实现单点登录(SSO)?

A: 可以在Token中添加设备ID，同一用户在不同设备上使用不同的Token，服务端可以管理所有设备的Token。
