# UserInfoClient 用户信息获取客户端文档

## 📋 概述

`UserInfoClient` 是一个**全局的用户信息获取工具类**,提供统一的用户信息访问接口:

1. **从ThreadLocal获取当前用户信息** (由Gateway注入到请求头)
2. **通过Dubbo调用account-management服务获取完整用户信息**

---

## 🏗️ 架构设计

### 完整的用户信息获取链路

```
┌─────────────────────────────────────────────────────────┐
│ 1. 前端发起请求 (携带JWT Token)                        │
└───────────────────────┬─────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 2. Gateway层                                            │
│    - 解析JWT Token                                      │
│    - 提取用户信息 (userId, username, roles, authorities)│
│    - 注入到请求头 (X-User-Id, X-Username, X-Roles等)   │
└───────────────────────┬─────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 3. 下游服务 (health-center-consumer)                    │
│    - UserInfoInterceptor 拦截请求                       │
│    - 从请求头提取用户信息                                │
│    - 存储到ThreadLocal (UserContext)                    │
└───────────────────────┬─────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 4. Controller层                                         │
│    - 注入 UserInfoClient                                │
│    - 调用 getCurrentUserId() 从ThreadLocal获取userId    │
│    - 调用 getCurrentPatientInfo() 通过Dubbo获取完整信息 │
└───────────────────────┬─────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 5. UserInfoClient                                       │
│    - getCurrentUserId() → UserInfoInterceptor.get()     │
│    - getCurrentPatientInfo() → Dubbo调用account服务     │
└─────────────────────────────────────────────────────────┘
```

---

## 📦 核心功能

### 1. 获取当前用户基本信息 (ThreadLocal)

这些信息由Gateway从JWT Token中提取并注入到ThreadLocal:

```java
@Autowired
private UserInfoClient userInfoClient;

// 获取当前用户ID
Long userId = userInfoClient.getCurrentUserId();

// 获取当前用户名
String username = userInfoClient.getCurrentUsername();

// 获取当前用户角色
String roles = userInfoClient.getCurrentUserRoles();  // "PATIENT,ADMIN"

// 获取当前用户权限
String authorities = userInfoClient.getCurrentUserAuthorities();  // "health:report:read,health:report:write"

// 获取链路追踪ID
String traceId = userInfoClient.getCurrentTraceId();
```

---

### 2. 获取当前用户完整信息 (Dubbo)

通过Dubbo调用account-management服务获取完整的患者信息:

```java
@Autowired
private UserInfoClient userInfoClient;

// 获取当前患者完整信息
PatientVO patient = userInfoClient.getCurrentPatientInfo();

// 使用患者信息
log.info("患者姓名: {}, 主治医生ID: {}", 
        patient.getNickname(), 
        patient.getAttendingDoctorId());
```

---

### 3. 根据ID获取患者信息

```java
// 根据患者ID获取患者信息
PatientVO patient = userInfoClient.getPatientInfoById(1001L);
```

---

### 4. 用户登录状态检查

```java
// 检查用户是否登录
if (!userInfoClient.isUserLoggedIn()) {
    throw new BusinessException("请先登录");
}

// 获取用户ID，未登录抛出异常
Long userId = userInfoClient.getCurrentUserIdOrThrow();
```

---

### 5. 角色和权限检查

```java
// 检查是否有PATIENT角色
if (userInfoClient.hasRole("PATIENT")) {
    // 患者特有逻辑
}

// 检查是否有health:report:write权限
if (userInfoClient.hasPermission("health:report:write")) {
    // 允许上传报告
}
```

---

## 🔧 在Controller中使用

### 改造前 (直接使用UserInfoInterceptor)

```java
@RestController
@RequestMapping("/health/report")
public class HealthReportController {
    
    @PostMapping("/upload")
    public Result uploadReport(...) {
        // ❌ 旧方式: 直接调用静态方法
        Long currentUserId = UserInfoInterceptor.getCurrentUserId();
        
        // 权限校验
        if (!patientId.equals(currentUserId)) {
            throw new BusinessException("无权上传他人报告");
        }
        
        // ...
    }
}
```

### 改造后 (使用UserInfoClient)

```java
@RestController
@RequestMapping("/health/report")
public class HealthReportController {
    
    @Autowired
    private UserInfoClient userInfoClient;  // ✅ 注入UserInfoClient
    
    @PostMapping("/upload")
    public Result uploadReport(...) {
        // ✅ 新方式: 通过UserInfoClient获取
        Long currentUserId = userInfoClient.getCurrentUserIdOrThrow();
        
        // 权限校验
        if (!patientId.equals(currentUserId)) {
            throw new BusinessException("无权上传他人报告");
        }
        
        // ...
    }
}
```

---

## 📊 完整使用示例

### 示例1: 上传报告 (权限校验)

```java
@RestController
@RequestMapping("/health/report")
public class HealthReportController {
    
    @Autowired
    private UserInfoClient userInfoClient;
    
    @PostMapping("/upload")
    @RequireRole("PATIENT")
    @RequirePermission("health:report:write")
    public Result<UploadReportResponse> uploadReport(
            @RequestParam Long patientId,
            @RequestParam MultipartFile file) {
        
        // 1. 获取当前用户ID
        Long currentUserId = userInfoClient.getCurrentUserIdOrThrow();
        
        // 2. 权限校验: 只能上传自己的报告
        if (!patientId.equals(currentUserId)) {
            throw new BusinessException("无权上传他人报告");
        }
        
        // 3. 业务逻辑
        // ...
        
        return Result.success();
    }
}
```

---

### 示例2: 查询报告 (获取完整患者信息)

```java
@GetMapping("/list")
@RequireRole("PATIENT")
@RequirePermission("health:report:read")
public Result<QueryReportListResponse> queryReportList() {
    
    // 1. 获取当前患者完整信息
    PatientVO patient = userInfoClient.getCurrentPatientInfo();
    
    // 2. 使用患者信息
    log.info("查询患者 {} 的报告列表, 主治医生: {}", 
            patient.getNickname(), 
            patient.getAttendingDoctorId());
    
    // 3. 构建查询条件
    QueryReportListRequest request = new QueryReportListRequest();
    request.setPatientId(patient.getId());
    request.setAttendingDoctorId(patient.getAttendingDoctorId());
    
    // 4. 查询报告
    // ...
    
    return Result.success();
}
```

---

### 示例3: 医生查看患者报告

```java
@GetMapping("/doctor/patient/{patientId}/reports")
@RequireRole("DOCTOR")
@RequirePermission("doctor:patient:read")
public Result<List<HealthReportVO>> getDoctorPatientReports(
        @PathVariable Long patientId) {
    
    // 1. 获取当前医生ID
    Long doctorId = userInfoClient.getCurrentUserIdOrThrow();
    
    // 2. 获取患者信息
    PatientVO patient = userInfoClient.getPatientInfoById(patientId);
    if (patient == null) {
        throw new BusinessException("患者不存在");
    }
    
    // 3. 校验: 只能查看自己的患者
    if (!doctorId.equals(patient.getAttendingDoctorId())) {
        throw new BusinessException("无权查看该患者的报告");
    }
    
    // 4. 查询报告
    // ...
    
    return Result.success();
}
```

---

## 🔄 与现有组件的关系

### 组件关系图

```
┌──────────────────────────────────────────────────────────┐
│ Gateway                                                  │
│ ├─ TraceAndJwtAuthFilter                                 │
│ │   └─ 解析JWT Token, 注入请求头                        │
│ └─ Request Headers:                                      │
│     ├─ X-User-Id: 1001                                   │
│     ├─ X-Username: zhangsan                              │
│     ├─ X-User-Roles: PATIENT                             │
│     └─ X-User-Authorities: health:report:read            │
└───────────────────────┬──────────────────────────────────┘
                        ↓
┌──────────────────────────────────────────────────────────┐
│ 下游服务 (health-center-consumer)                        │
│ ├─ UserInfoInterceptor (拦截器)                          │
│ │   ├─ 从请求头提取用户信息                              │
│ │   └─ 存储到 UserContext (ThreadLocal)                 │
│ └─ UserInfoClient (客户端)                               │
│     ├─ getCurrentUserId() → UserContext.getUserId()      │
│     └─ getCurrentPatientInfo() → Dubbo调用account服务    │
└──────────────────────────────────────────────────────────┘
```

---

## ⚠️ 注意事项

### 1. 依赖account-api

UserInfoClient依赖`account-api`模块,需要在POM中添加:

```xml
<dependency>
    <groupId>com.zixin</groupId>
    <artifactId>account-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. Dubbo超时配置

UserInfoClient内部通过Dubbo调用account服务,默认超时3秒:

```java
@DubboReference(version = "1.0.0", check = false, timeout = 3000)
private UserIdentityAPI userIdentityAPI;
```

### 3. 异常处理

```java
// getCurrentUserIdOrThrow() 会抛出异常
try {
    Long userId = userInfoClient.getCurrentUserIdOrThrow();
} catch (IllegalStateException e) {
    // 用户未登录
}

// getPatientInfoById() 返回null表示失败
PatientVO patient = userInfoClient.getPatientInfoById(1001L);
if (patient == null) {
    // 患者不存在或查询失败
}
```

---

## ✅ 优势对比

### 改造前的问题

1. ❌ 直接调用 `UserInfoInterceptor.getCurrentUserId()` 静态方法
2. ❌ 无法获取完整的患者信息
3. ❌ 每个地方都要写Dubbo调用account服务的代码
4. ❌ 代码重复,不易维护

### 改造后的优势

1. ✅ 统一的用户信息获取接口
2. ✅ 封装了Dubbo调用逻辑
3. ✅ 支持获取完整患者信息
4. ✅ 代码复用性高
5. ✅ 易于测试 (可以Mock UserInfoClient)

---

## 📝 API列表

| 方法 | 返回类型 | 说明 | 异常 |
|-----|---------|------|------|
| `getCurrentUserId()` | `Long` | 获取当前用户ID | 未登录返回null |
| `getCurrentUsername()` | `String` | 获取当前用户名 | 未登录返回null |
| `getCurrentUserRoles()` | `String` | 获取当前用户角色 | 未登录返回null |
| `getCurrentUserAuthorities()` | `String` | 获取当前用户权限 | 未登录返回null |
| `getCurrentTraceId()` | `String` | 获取链路追踪ID | - |
| `getCurrentPatientInfo()` | `PatientVO` | 获取当前患者完整信息 | 未登录抛异常 |
| `getPatientInfoById(Long)` | `PatientVO` | 根据ID获取患者信息 | 失败返回null |
| `isUserLoggedIn()` | `boolean` | 检查用户是否登录 | - |
| `getCurrentUserIdOrThrow()` | `Long` | 获取用户ID,未登录抛异常 | 未登录抛异常 |
| `hasRole(String)` | `boolean` | 检查是否有指定角色 | - |
| `hasPermission(String)` | `boolean` | 检查是否有指定权限 | - |

---

## ✅ 实现清单

- [x] 创建 `UserInfoClient` 类
- [x] 实现 `getCurrentUserId()` 方法
- [x] 实现 `getCurrentUsername()` 方法
- [x] 实现 `getCurrentUserRoles()` 方法
- [x] 实现 `getCurrentUserAuthorities()` 方法
- [x] 实现 `getCurrentTraceId()` 方法
- [x] 实现 `getCurrentPatientInfo()` 方法 (Dubbo调用)
- [x] 实现 `getPatientInfoById()` 方法
- [x] 实现 `isUserLoggedIn()` 方法
- [x] 实现 `getCurrentUserIdOrThrow()` 方法
- [x] 实现 `hasRole()` 方法
- [x] 实现 `hasPermission()` 方法
- [x] Controller层改造使用UserInfoClient
- [x] UserInfoClient文档

---

现在项目拥有了**统一的用户信息获取客户端**! 🎉
