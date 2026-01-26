# JWT双Token认证架构

## 系统架构图

```mermaid
graph TB
    Client["客户端<br/>(Web/Mobile)"]
    Gateway["API网关"]
    AuthService["认证服务<br/>(auth-provider)"]
    BusinessService["业务服务"]
    Redis["Redis<br/>(Token存储)"]
    DB["数据库<br/>(用户/权限)"]
    
    Client -->|1. 登录请求| Gateway
    Gateway -->|2. 转发| AuthService
    AuthService -->|3. 验证用户| DB
    AuthService -->|4. 生成Token对| AuthService
    AuthService -->|5. 存储RefreshToken| Redis
    AuthService -->|6. 返回Token| Gateway
    Gateway -->|7. 返回Token| Client
    
    Client -->|8. API请求+AccessToken| Gateway
    Gateway -->|9. 验证Token| AuthService
    AuthService -->|10. 检查黑名单| Redis
    AuthService -->|11. 返回验证结果| Gateway
    Gateway -->|12. 转发请求| BusinessService
    BusinessService -->|13. 返回响应| Gateway
    Gateway -->|14. 返回响应| Client
    
    Client -->|15. RefreshToken请求| Gateway
    Gateway -->|16. 转发| AuthService
    AuthService -->|17. 验证RefreshToken| Redis
    AuthService -->|18. 生成新Token对| AuthService
    AuthService -->|19. 返回新Token| Gateway
    Gateway -->|20. 返回新Token| Client
    
    style AuthService fill:#e1f5ff
    style Redis fill:#ffe1e1
    style DB fill:#e1ffe1
```

## Token生成流程

```mermaid
sequenceDiagram
    participant C as 客户端
    participant A as 认证服务
    participant R as Redis
    participant D as 数据库
    
    C->>A: 1. 登录(用户名+密码)
    A->>D: 2. 验证用户凭证
    D-->>A: 3. 返回用户信息
    A->>D: 4. 查询用户权限
    D-->>A: 5. 返回权限列表
    A->>R: 6. 缓存用户权限
    A->>A: 7. 生成AccessToken<br/>(包含userId+roles+permissions)
    A->>A: 8. 生成RefreshToken<br/>(包含userId+tokenId)
    A->>R: 9. 存储RefreshToken
    A-->>C: 10. 返回Token对<br/>(AccessToken+RefreshToken)
    
    Note over A: AccessToken有效期: 30分钟<br/>RefreshToken有效期: 7天
```

## Token验证流程

```mermaid
sequenceDiagram
    participant C as 客户端
    participant G as API网关
    participant F as JWT过滤器
    participant R as Redis
    participant B as 业务服务
    
    C->>G: 1. API请求<br/>Header: Authorization: Bearer {token}
    G->>F: 2. 提取并验证Token
    F->>F: 3. JWT签名验证
    F->>R: 4. 检查Token黑名单
    
    alt Token有效
        R-->>F: 5. Token未被撤销
        F->>F: 6. 解析用户信息和权限
        F->>F: 7. 设置Security上下文
        F->>B: 8. 转发请求
        B->>B: 9. 权限检查(@RequireRole/@RequirePermission)
        B-->>G: 10. 返回业务数据
        G-->>C: 11. 返回响应
    else Token无效
        R-->>F: Token已被撤销
        F-->>G: 返回401未授权
        G-->>C: 返回401错误
    end
```

## Token刷新流程

```mermaid
sequenceDiagram
    participant C as 客户端
    participant A as 认证服务
    participant R as Redis
    
    C->>A: 1. 刷新请求<br/>(RefreshToken)
    A->>A: 2. 解析RefreshToken
    A->>R: 3. 验证RefreshToken<br/>Key: auth:refresh_token:{userId}:{tokenId}
    
    alt RefreshToken有效
        R-->>A: 4. RefreshToken存在且匹配
        A->>R: 5. 获取用户权限缓存
        A->>A: 6. 生成新AccessToken
        A->>A: 7. 生成新RefreshToken
        A->>R: 8. 删除旧RefreshToken
        A->>R: 9. 存储新RefreshToken
        A-->>C: 10. 返回新Token对
    else RefreshToken无效
        R-->>A: RefreshToken不存在或已过期
        A-->>C: 返回401错误<br/>(需要重新登录)
    end
    
    Note over A,R: 刷新时撤销旧RefreshToken<br/>防止Token重放攻击
```

## 登出流程

```mermaid
sequenceDiagram
    participant C as 客户端
    participant A as 认证服务
    participant R as Redis
    
    C->>A: 1. 登出请求<br/>(AccessToken)
    A->>A: 2. 解析Token获取tokenId
    A->>R: 3. 将AccessToken加入黑名单<br/>Key: auth:blacklist:{tokenId}<br/>TTL: Token剩余有效期
    A->>A: 4. 解析Token获取userId
    A->>R: 5. 删除所有RefreshToken<br/>Key: auth:refresh_token:{userId}:*
    A-->>C: 6. 返回登出成功
    
    Note over R: Token黑名单确保已登出的<br/>AccessToken立即失效
```

## 权限检查流程

```mermaid
graph TD
    A["请求到达"] --> B{"JWT过滤器验证"}
    B -->|无Token| C["返回401"]
    B -->|有Token| D{"Token有效?"}
    D -->|否| C
    D -->|是| E["提取用户信息和权限"]
    E --> F["设置Security上下文"]
    F --> G["到达业务方法"]
    G --> H{"有权限注解?"}
    H -->|无| I["执行业务逻辑"]
    H -->|@RequireRole| J{"检查角色"}
    H -->|@RequirePermission| K{"检查权限"}
    H -->|@PreAuthorize| L{"SpEL表达式验证"}
    J -->|通过| I
    J -->|失败| M["返回403"]
    K -->|通过| I
    K -->|失败| M
    L -->|通过| I
    L -->|失败| M
    I --> N["返回响应"]
    
    style C fill:#ffcccc
    style M fill:#ffcccc
    style N fill:#ccffcc
```

## Redis数据结构

```mermaid
graph LR
    subgraph "用户权限缓存"
        P1["Key: auth:permissions:1001<br/>Type: Set<br/>Value: {user:read, user:write, ...}<br/>TTL: 30分钟"]
    end
    
    subgraph "Refresh Token存储"
        R1["Key: auth:refresh_token:1001:uuid-1<br/>Type: String<br/>Value: {refreshToken}<br/>TTL: 7天"]
        R2["Key: auth:refresh_token:1001:uuid-2<br/>Type: String<br/>Value: {refreshToken}<br/>TTL: 7天"]
    end
    
    subgraph "Token黑名单"
        B1["Key: auth:blacklist:token-id-1<br/>Type: String<br/>Value: 1<br/>TTL: Token剩余有效期"]
        B2["Key: auth:blacklist:token-id-2<br/>Type: String<br/>Value: 1<br/>TTL: Token剩余有效期"]
    end
    
    style P1 fill:#e1f5ff
    style R1 fill:#ffe1e1
    style R2 fill:#ffe1e1
    style B1 fill:#fff3cd
    style B2 fill:#fff3cd
```

## Token内容对比

| 字段 | Access Token | Refresh Token |
|------|-------------|---------------|
| sub (用户ID) | ✅ | ✅ |
| type (Token类型) | access | refresh |
| roles (角色) | ✅ | ❌ |
| authorities (权限) | ✅ | ❌ |
| jti (Token ID) | ✅ (可选) | ✅ (必需) |
| iat (签发时间) | ✅ | ✅ |
| exp (过期时间) | 30分钟 | 7天 |
| 存储位置 | 不存储 | Redis |
| 用途 | API访问 | 刷新Token |

## 安全特性

1. **双Token机制**
   - Access Token: 短期有效，降低泄露风险
   - Refresh Token: 长期有效，减少登录次数
   - 分离存储和验证逻辑

2. **Token黑名单**
   - 支持即时撤销Token
   - 实现登出功能
   - 防止已泄露Token被滥用

3. **Refresh Token轮换**
   - 刷新时生成新的Refresh Token
   - 撤销旧的Refresh Token
   - 防止Token重放攻击

4. **权限缓存**
   - Redis缓存用户权限
   - 减少数据库查询
   - 支持权限即时更新

5. **RSA签名**
   - 使用RSA算法签名
   - 公钥验证，私钥签名
   - 支持密钥轮换

## 核心优势

✅ **安全性高**: 双Token机制，降低泄露风险  
✅ **性能好**: 权限缓存，减少数据库查询  
✅ **可扩展**: 支持自定义权限注解和Spring Security  
✅ **易用性**: 提供完整的API和示例代码  
✅ **灵活性**: 支持角色和权限的细粒度控制  
✅ **可维护**: 代码结构清晰，注释完整
