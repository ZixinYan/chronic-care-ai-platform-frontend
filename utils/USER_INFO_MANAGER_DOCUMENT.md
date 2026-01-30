# UserInfoManager 全局用户信息管理器文档

## 📋 概述

`UserInfoManager` 是一个**全局的用户信息管理器**,基于ThreadLocal存储当前请求的用户完整信息。

### 核心特点

1. ✅ **无需依赖业务服务**: utils模块独立,不依赖account-management等业务服务
2. ✅ **用户信息由Gateway注入**: Gateway解析JWT Token后注入请求头
3. ✅ **拦截器提取到ThreadLocal**: UserInfoExtractInterceptor从请求头提取并存储
4. ✅ **所有服务直接获取**: 各业务服务通过UserInfoManager.getUserId()直接获取
5. ✅ **不查询数据库**: 用户信息在JWT Token中已包含,无需每次查询

---

## 🏗️ 完整架构设计

### 用户信息流转链路

```
┌─────────────────────────────────────────────────────────┐
│ 1. 用户登录                                             │
│    ├─ 前端提交账号密码                                  │
│    ├─ auth-server验证成功                               │
│    ├─ 查询account-management获取完整用户信息            │
│    └─ 生成JWT Token (包含完整用户信息)                  │
│        - userId                                         │
│        - username                                       │
│        - userType (1-患者, 2-医生, 3-管理员)            │
│        - roles                                          │
│        - authorities                                    │
│        - realName                                       │
│        - nickname                                       │
│        - phone                                          │
│        - email                                          │
│        - attendingDoctorId (患者专属)                   │
│        - departmentId (医生专属)                        │
└───────────────────────┬─────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 2. 前端请求 (携带JWT Token)                             │
│    Authorization: Bearer <JWT_TOKEN>                    │
└───────────────────────┬─────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 3. Gateway层                                            │
│    ├─ TraceAndJwtAuthFilter 解析JWT Token               │
│    ├─ 提取用户完整信息                                  │
│    └─ 注入到请求头:                                     │
│        X-User-Id: 1001                                  │
│        X-Username: zhangsan                             │
│        X-User-Type: 1                                   │
│        X-User-Roles: PATIENT                            │
│        X-User-Authorities: health:report:read           │
│        X-Real-Name: 张三                                │
│        X-Nickname: 小张                                 │
│        X-Phone: 13800138000                             │
│        X-Email: zhangsan@example.com                    │
│        X-Attending-Doctor-Id: 2001                      │
│        X-Trace-Id: 123e4567-e89b-12d3-a456-426614174000 │
└───────────────────────┬─────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 4. 下游服务 (health-center-consumer)                    │
│    ├─ UserInfoExtractInterceptor拦截请求                │
│    ├─ 从请求头提取用户信息                              │
│    ├─ 构建UserInfoContext对象                           │
│    └─ 存储到ThreadLocal (UserInfoManager)               │
└───────────────────────┬─────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 5. Controller层 - 直接获取用户信息                      │
│    Long userId = UserInfoManager.getUserId();           │
│    String username = UserInfoManager.getUsername();     │
│    Long doctorId = UserInfoManager.getAttendingDoctorId();│
│                                                         │
│    ✅ 无需注入任何依赖                                  │
│    ✅ 无需调用Dubbo服务                                 │
│    ✅ 无需查询数据库                                    │
└─────────────────────────────────────────────────────────┘
```

---

## 🔧 核心组件

### 1. UserInfoContext (用户上下文对象)

```java
@Data
@Builder
public class UserInfoContext {
    private Long userId;              // 用户ID
    private String username;          // 用户名
    private String roles;             // 角色列表
    private String authorities;       // 权限列表
    private String traceId;           // 链路追踪ID
    private Integer userType;         // 用户类型 (1-患者, 2-医生, 3-管理员)
    private String realName;          // 真实姓名
    private String nickname;          // 昵称
    private String phone;             // 手机号
    private String email;             // 邮箱
    private Long attendingDoctorId;   // 主治医生ID (仅患者)
    private Long departmentId;        // 科室ID (仅医生)
    private String requestIp;         // 请求IP
    private Long requestTime;         // 请求时间戳
}
```

---

### 2. UserInfoManager (全局管理器)

```java
public class UserInfoManager {
    
    private static final ThreadLocal<UserInfoContext> USER_CONTEXT_HOLDER = new ThreadLocal<>();
    
    // ========== 核心方法 ==========
    
    // 设置用户上下文
    public static void setUserContext(UserInfoContext context)
    
    // 获取用户上下文
    public static UserInfoContext getUserContext()
    
    // 清理用户上下文 (防止内存泄漏)
    public static void clearUserContext()
    
    // ========== 便捷获取方法 ==========
    
    public static Long getUserId()                  // 获取用户ID
    public static String getUsername()              // 获取用户名
    public static String getRoles()                 // 获取角色
    public static String getAuthorities()           // 获取权限
    public static String getTraceId()               // 获取链路ID
    public static Integer getUserType()             // 获取用户类型
    public static String getRealName()              // 获取真实姓名
    public static String getNickname()              // 获取昵称
    public static String getPhone()                 // 获取手机号
    public static String getEmail()                 // 获取邮箱
    public static Long getAttendingDoctorId()       // 获取主治医生ID
    public static Long getDepartmentId()            // 获取科室ID
    public static String getRequestIp()             // 获取请求IP
    public static Long getRequestTime()             // 获取请求时间
    
    // ========== 工具方法 ==========
    
    public static boolean isUserLoggedIn()          // 检查是否登录
    public static Long getUserIdOrThrow()           // 获取用户ID,未登录抛异常
    public static boolean hasRole(String role)      // 检查是否有指定角色
    public static boolean hasPermission(String)     // 检查是否有指定权限
    public static boolean isPatient()               // 是否是患者
    public static boolean isDoctor()                // 是否是医生
    public static boolean isAdmin()                 // 是否是管理员
}
```

---

### 3. UserInfoExtractInterceptor (拦截器)

```java
@Component
public class UserInfoExtractInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        // 1. 从请求头提取用户信息
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        // ... 提取所有字段
        
        // 2. 构建UserInfoContext对象
        UserInfoContext context = UserInfoContext.builder()
                .userId(...)
                .username(...)
                .build();
        
        // 3. 存储到ThreadLocal
        UserInfoManager.setUserContext(context);
        
        return true;
    }
    
    @Override
    public void afterCompletion(...) {
        // 清理ThreadLocal，防止内存泄漏
        UserInfoManager.clearUserContext();
    }
}
```

---

## 📝 使用示例

### 示例1: 获取当前用户基本信息

```java
@RestController
@RequestMapping("/health/report")
public class HealthReportController {
    
    @PostMapping("/upload")
    public Result upload(@RequestParam Long patientId) {
        // ✅ 直接获取用户信息,无需注入任何依赖
        Long userId = UserInfoManager.getUserId();
        String username = UserInfoManager.getUsername();
        String traceId = UserInfoManager.getTraceId();
        
        log.info("用户 {} (ID: {}) 上传报告, traceId: {}", username, userId, traceId);
        
        // 权限校验
        if (!patientId.equals(userId)) {
            throw new BusinessException("无权上传他人报告");
        }
        
        // ...
    }
}
```

---

### 示例2: 获取患者专属信息

```java
@GetMapping("/patient/info")
public Result getPatientInfo() {
    // 检查是否是患者
    if (!UserInfoManager.isPatient()) {
        throw new BusinessException("只有患者才能访问");
    }
    
    // 获取患者专属信息
    Long userId = UserInfoManager.getUserId();
    String realName = UserInfoManager.getRealName();
    String phone = UserInfoManager.getPhone();
    Long attendingDoctorId = UserInfoManager.getAttendingDoctorId();
    
    log.info("患者信息 - ID: {}, 姓名: {}, 手机号: {}, 主治医生ID: {}", 
            userId, realName, phone, attendingDoctorId);
    
    // 构建响应
    return Result.success(Map.of(
        "userId", userId,
        "realName", realName,
        "phone", phone,
        "attendingDoctorId", attendingDoctorId
    ));
}
```

---

### 示例3: 医生查看患者报告

```java
@GetMapping("/doctor/patient/{patientId}/reports")
@RequireRole("DOCTOR")
public Result getDoctorPatientReports(@PathVariable Long patientId) {
    // 获取当前医生信息
    Long doctorId = UserInfoManager.getUserIdOrThrow();
    Long departmentId = UserInfoManager.getDepartmentId();
    String doctorName = UserInfoManager.getRealName();
    
    log.info("医生 {} (ID: {}, 科室: {}) 查看患者 {} 的报告", 
            doctorName, doctorId, departmentId, patientId);
    
    // 查询报告...
    
    return Result.success();
}
```

---

### 示例4: 权限检查

```java
@PostMapping("/admin/operation")
public Result adminOperation() {
    // 检查是否登录
    if (!UserInfoManager.isUserLoggedIn()) {
        throw new BusinessException("请先登录");
    }
    
    // 检查角色
    if (!UserInfoManager.hasRole("ADMIN")) {
        throw new BusinessException("需要管理员权限");
    }
    
    // 检查权限
    if (!UserInfoManager.hasPermission("admin:operation")) {
        throw new BusinessException("权限不足");
    }
    
    // 管理员操作...
}
```

---

## 🔄 与Gateway的集成

### Gateway注入用户信息

```java
// gateway/src/main/java/com/zixin/gateway/config/TraceAndJwtAuthFilter.java

// 1. 解析JWT Token
ValidateJwtResponse response = authAPI.validateJwt(token);

// 2. 提取用户信息
Long userId = response.getUserId();
String username = response.getUsername();
Integer userType = response.getUserType();
// ... 所有字段

// 3. 注入到请求头
ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
        .header("X-User-Id", String.valueOf(userId))
        .header("X-Username", username)
        .header("X-User-Type", String.valueOf(userType))
        // ... 所有字段
        .build();
```

---

## ✅ 优势对比

### 改造前的问题

1. ❌ 需要通过Dubbo调用account服务获取用户信息
2. ❌ 每次都要查询数据库
3. ❌ 网络开销大,响应慢
4. ❌ account服务压力大
5. ❌ utils模块依赖业务服务,违背分层架构

### 改造后的优势

1. ✅ 用户信息在JWT Token中,Gateway解析后直接注入
2. ✅ 无需查询数据库,性能极高
3. ✅ 所有服务直接从ThreadLocal获取,零网络开销
4. ✅ utils模块独立,不依赖任何业务服务
5. ✅ 代码简洁,使用方便

---

## 📊 性能对比

### 改造前 (Dubbo调用)

```
Controller获取用户信息
  ↓
Dubbo调用account-provider (网络开销: 10-50ms)
  ↓
查询数据库 (数据库查询: 5-20ms)
  ↓
返回用户信息
  ↓
总耗时: 15-70ms
```

### 改造后 (ThreadLocal)

```
Controller获取用户信息
  ↓
UserInfoManager.getUserId() (内存读取: <1ms)
  ↓
总耗时: <1ms
```

**性能提升**: 15-70倍 🚀

---

## ⚠️ 注意事项

### 1. 必须注册拦截器

每个使用UserInfoManager的服务都必须注册`UserInfoExtractInterceptor`:

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private UserInfoExtractInterceptor userInfoExtractInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userInfoExtractInterceptor)
                .addPathPatterns("/**");
    }
}
```

### 2. 防止ThreadLocal内存泄漏

拦截器的`afterCompletion`方法会自动清理:

```java
@Override
public void afterCompletion(...) {
    UserInfoManager.clearUserContext();  // 必须调用
}
```

### 3. JWT Token必须包含完整信息

auth-server生成JWT Token时必须包含所有用户信息:

```java
// 错误: JWT只包含userId
{
  "userId": 1001
}

// 正确: JWT包含完整信息
{
  "userId": 1001,
  "username": "zhangsan",
  "userType": 1,
  "roles": ["PATIENT"],
  "authorities": ["health:report:read"],
  "realName": "张三",
  "nickname": "小张",
  "phone": "13800138000",
  "attendingDoctorId": 2001
}
```

---

## 📚 完整的API列表

| 方法 | 返回类型 | 说明 |
|-----|---------|------|
| `getUserId()` | `Long` | 获取用户ID |
| `getUsername()` | `String` | 获取用户名 |
| `getRoles()` | `String` | 获取角色列表 |
| `getAuthorities()` | `String` | 获取权限列表 |
| `getTraceId()` | `String` | 获取链路追踪ID |
| `getUserType()` | `Integer` | 获取用户类型 |
| `getRealName()` | `String` | 获取真实姓名 |
| `getNickname()` | `String` | 获取昵称 |
| `getPhone()` | `String` | 获取手机号 |
| `getEmail()` | `String` | 获取邮箱 |
| `getAttendingDoctorId()` | `Long` | 获取主治医生ID |
| `getDepartmentId()` | `Long` | 获取科室ID |
| `getRequestIp()` | `String` | 获取请求IP |
| `getRequestTime()` | `Long` | 获取请求时间戳 |
| `isUserLoggedIn()` | `boolean` | 检查是否登录 |
| `getUserIdOrThrow()` | `Long` | 获取用户ID,未登录抛异常 |
| `hasRole(String)` | `boolean` | 检查是否有指定角色 |
| `hasPermission(String)` | `boolean` | 检查是否有指定权限 |
| `isPatient()` | `boolean` | 是否是患者 |
| `isDoctor()` | `boolean` | 是否是医生 |
| `isAdmin()` | `boolean` | 是否是管理员 |

---

## ✅ 实现清单

- [x] 创建 `UserInfoContext` 用户上下文对象
- [x] 创建 `UserInfoManager` 全局管理器
- [x] 创建 `UserInfoExtractInterceptor` 拦截器
- [x] Gateway层注入用户完整信息到请求头
- [x] health-center-consumer注册拦截器
- [x] Controller层改造使用UserInfoManager
- [x] 完整的使用文档

---

## 🎉 总结

现在项目拥有了**零依赖的全局用户信息管理器**:

1. ✅ **无需依赖业务服务**: utils模块完全独立
2. ✅ **性能极高**: 从ThreadLocal读取,<1ms
3. ✅ **使用简单**: `UserInfoManager.getUserId()`
4. ✅ **功能完善**: 21个便捷方法
5. ✅ **架构清晰**: Gateway注入 → 拦截器提取 → 业务使用

完美解决了用户信息获取问题! 🚀
