# Auth Server - JWT双Token认证系统

## 概述

基于JWT的双Token认证机制，提供完整的用户认证、授权和权限管理功能。

## 功能特性

### 1. 双Token机制

- **Access Token**: 短期有效(默认30分钟)，用于API访问
  - 包含用户ID、角色和权限信息
  - 存储在内存中，无需查询数据库
  
- **Refresh Token**: 长期有效(默认7天)，用于刷新Access Token
  - 只包含用户ID和Token ID
  - 存储在Redis中，支持撤销

### 2. 角色和权限控制

- **角色(Roles)**: 用户的角色标识，如ADMIN、USER等
- **权限(Permissions)**: 细粒度的权限控制，如user:read、user:write等
- 支持基于注解的权限控制
- 支持Spring Security的方法级权限控制

### 3. Token安全特性

- **Token黑名单**: 支持Token撤销(登出功能)
- **Token验证**: 完整的Token验证机制
- **权限缓存**: Redis缓存用户权限，减少数据库查询
- **RSA签名**: 使用RSA算法签名和验证Token

## 核心组件

### 1. API接口

#### JwtAPI
```java
// 生成Token对
GenTokenResponse genToken(GenTokenRequest request);
// 刷新Token
RefreshTokenResponse refreshToken(RefreshTokenRequest request);
```

#### TokenValidationAPI
```java
// 验证Token
ValidateTokenResponse validateToken(ValidateTokenRequest request);
// 撤销Token
ValidateTokenResponse revokeToken(ValidateTokenRequest request);
```

### 2. 核心类

#### JwtUtils
Token生成、验证和管理的核心工具类:
- `generateAccessToken()`: 生成Access Token
- `generateRefreshToken()`: 生成Refresh Token
- `generateTokenPair()`: 生成Token对
- `validateAndParseToken()`: 验证并解析Token
- `validateRefreshToken()`: 验证Refresh Token
- `blacklistToken()`: 将Token加入黑名单
- `revokeRefreshToken()`: 撤销Refresh Token

#### JwtAuthenticationFilter
JWT认证过滤器，自动验证请求中的Token并设置Security上下文

#### AuthorizationAspect
权限控制切面，拦截@RequireRole和@RequirePermission注解的方法

### 3. 配置类

#### JwtConfig
JWT编码器和解码器配置，使用RSA密钥对

#### SecurityConfig
Spring Security配置，配置无状态会话

#### RedisConfig
Redis配置，用于缓存权限和Refresh Token

## 使用示例

### 1. 生成Token

```java
// 构造请求
GenTokenRequest request = new GenTokenRequest();
request.setUserId(1001L);
request.setUsername("zhangsan");
request.setRoles(Arrays.asList("ADMIN", "USER"));

// 调用服务
GenTokenResponse response = jwtAPI.genToken(request);

// 获取Token
String accessToken = response.getAccessToken();
String refreshToken = response.getRefreshToken();
```

### 2. 刷新Token

```java
// 构造请求
RefreshTokenRequest request = new RefreshTokenRequest();
request.setRefreshToken(refreshToken);

// 调用服务
RefreshTokenResponse response = jwtAPI.refreshToken(request);

// 获取新Token
String newAccessToken = response.getAccessToken();
String newRefreshToken = response.getRefreshToken();
```

### 3. 验证Token

```java
// 构造请求
ValidateTokenRequest request = new ValidateTokenRequest();
request.setToken(accessToken);

// 调用服务
ValidateTokenResponse response = tokenValidationAPI.validateToken(request);

// 检查结果
if (response.getValid()) {
    Long userId = response.getUserId();
    List<String> roles = response.getRoles();
    List<String> authorities = response.getAuthorities();
}
```

### 4. 撤销Token(登出)

```java
// 构造请求
ValidateTokenRequest request = new ValidateTokenRequest();
request.setToken(accessToken);

// 调用服务
ValidateTokenResponse response = tokenValidationAPI.revokeToken(request);
```

### 5. 使用权限注解

```java
// 需要ADMIN角色
@RequireRole("ADMIN")
public void adminOnlyMethod() {
    // ...
}

// 需要ADMIN或MANAGER角色之一
@RequireRole(value = {"ADMIN", "MANAGER"}, requireAll = false)
public void managerMethod() {
    // ...
}

// 需要同时拥有ADMIN和SUPER_USER角色
@RequireRole(value = {"ADMIN", "SUPER_USER"}, requireAll = true)
public void superAdminMethod() {
    // ...
}

// 需要user:write权限
@RequirePermission("user:write")
public void updateUser() {
    // ...
}

// 需要user:read和user:write权限
@RequirePermission(value = {"user:read", "user:write"}, requireAll = true)
public void manageUser() {
    // ...
}
```

### 6. 在HTTP请求中使用Token

```bash
# 使用Access Token访问API
curl -H "Authorization: Bearer {accessToken}" http://localhost:8082/api/xxx
```

## 配置说明

### application.properties

```properties
# JWT配置
jwt.access-token.expiration=1800        # Access Token有效期(秒)
jwt.refresh-token.expiration=604800     # Refresh Token有效期(秒)
jwt.permissions.cache-ttl=1800          # 权限缓存有效期(秒)
jwt.key-size=2048                       # RSA密钥大小

# Redis配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.database=0
```

## Redis存储结构

### 1. 权限缓存
```
Key: auth:permissions:{userId}
Type: Set
Value: ["user:read", "user:write", ...]
TTL: jwt.permissions.cache-ttl
```

### 2. Refresh Token
```
Key: auth:refresh_token:{userId}:{tokenId}
Type: String
Value: {refreshToken}
TTL: jwt.refresh-token.expiration
```

### 3. Token黑名单
```
Key: auth:blacklist:{tokenId}
Type: String
Value: "1"
TTL: Token过期时间
```

## 安全建议

### 生产环境配置

1. **密钥管理**
   - 使用配置中心或密钥管理服务存储RSA密钥
   - 定期轮换密钥
   - 不要在代码中硬编码密钥

2. **Token有效期**
   - Access Token: 建议15-30分钟
   - Refresh Token: 建议1-7天
   - 根据业务需求调整

3. **HTTPS**
   - 生产环境必须使用HTTPS
   - 防止Token被中间人拦截

4. **刷新Token策略**
   - 使用Refresh Token时撤销旧的Refresh Token
   - 检测Refresh Token重放攻击

5. **权限缓存**
   - 权限变更时及时清除缓存
   - 设置合理的缓存TTL

## 依赖

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>

<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 扩展功能

### 可以进一步实现的功能

1. **设备管理**: 记录用户登录的设备，支持踢出特定设备
2. **IP白名单**: 限制特定IP访问
3. **访问频率限制**: 防止暴力破解
4. **审计日志**: 记录所有认证和授权操作
5. **多租户支持**: 支持多租户场景
6. **SSO单点登录**: 集成OAuth2/OpenID Connect
7. **动态权限**: 支持运行时动态修改权限
8. **权限继承**: 支持角色继承和权限组

## 故障排查

### 常见问题

1. **Token验证失败**
   - 检查Token是否过期
   - 检查Token格式是否正确
   - 检查RSA密钥是否匹配

2. **权限检查失败**
   - 检查用户是否有对应的角色或权限
   - 检查Redis中的权限缓存是否正确
   - 检查注解使用是否正确

3. **Refresh Token失效**
   - 检查Redis中是否存在对应的Refresh Token
   - 检查Refresh Token是否已过期
   - 检查是否已被撤销

## 许可证

MIT License
