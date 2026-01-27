# 权限实体类设计文档

## 概述

本文档说明account-management服务中角色和权限实体类的设计,以及如何与`@RequireRole`和`@RequirePermission`注解配合使用。

## 数据库表结构

### 1. care_platform_role - 角色表

```sql
CREATE TABLE care_platform_role (
    role_id BIGINT PRIMARY KEY,
    code INT NOT NULL COMMENT '角色代码: 1-DOCTOR, 2-PATIENT, 3-FAMILY',
    name VARCHAR(50) COMMENT '角色名称',
    description VARCHAR(200) COMMENT '角色描述',
    action INT COMMENT '基础权限级别: 1-READ, 2-WRITE, 3-ALL',
    deleted INT DEFAULT 0,
    version INT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);
```

### 2. care_platform_role_permission - 角色权限关联表

```sql
CREATE TABLE care_platform_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code INT NOT NULL COMMENT '角色代码',
    action_code INT NOT NULL COMMENT '权限代码: 1-READ, 2-WRITE, 3-ALL',
    create_time DATETIME
);
```

### 3. care_platform_permission - 权限表(可选)

```sql
CREATE TABLE care_platform_permission (
    permission_id BIGINT PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL COMMENT '权限代码: user:read, medical:record:write',
    name VARCHAR(100) COMMENT '权限名称',
    description VARCHAR(200) COMMENT '权限描述',
    category VARCHAR(50) COMMENT '权限分类',
    deleted INT DEFAULT 0,
    version INT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);
```

## 实体类设计

### 1. Role - 角色实体

```java
@Data
@TableName("care_platform_role")
public class Role {
    @TableId(type = IdType.ASSIGN_ID)
    private Long roleId;
    
    // 角色代码: 1-DOCTOR, 2-PATIENT, 3-FAMILY
    private Integer code;
    
    private String name;
    private String description;
    
    // 基础权限级别: 1-READ, 2-WRITE, 3-ALL
    private Integer action;
    
    @TableLogic
    private Integer deleted;
    
    @Version
    private Integer version;
    
    private Date createTime;
    private Date updateTime;
    
    // 获取角色名称(用于@RequireRole注解)
    // 返回: "DOCTOR", "PATIENT", "FAMILY"
    public String getRoleCodeName() {
        return RoleCode.fromCode(this.code).name();
    }
}
```

### 2. RolePermission - 角色权限关联

```java
@Data
@TableName("care_platform_role_permission")
public class RolePermission {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 角色代码: 1-DOCTOR, 2-PATIENT, 3-FAMILY
    private Integer roleCode;
    
    // 权限代码: 1-READ, 2-WRITE, 3-ALL
    private Integer actionCode;
    
    private Date createTime;
    
    // 获取角色名称
    public String getRoleName() {
        return RoleCode.fromCode(this.roleCode).name();
    }
}
```

## 枚举定义

### RoleCode - 角色枚举

```java
public enum RoleCode {
    DOCTOR(1, "医生"),
    PATIENT(2, "患者"),
    FAMILY(3, "家属");
    
    private final int code;
    private final String description;
    
    public static RoleCode fromCode(Integer code) {
        for (RoleCode roleCode : values()) {
            if (roleCode.code == code) {
                return roleCode;
            }
        }
        throw new IllegalArgumentException("Unknown role code: " + code);
    }
}
```

### Action - 权限级别枚举

```java
public enum Action {
    READ(1, "可读"),
    WRITE(2, "可编辑"),
    ALL(3, "拥有全部权限");
    
    private final int code;
    private final String description;
    
    public static Action fromCode(Integer code) {
        for (Action action : values()) {
            if (action.code == code) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown action code: " + code);
    }
}
```

## 权限映射关系

### 当前设计(基于Action枚举)

```
角色        -> 权限级别      -> 展开后的权限
DOCTOR     -> ALL          -> user:read, user:write, medical:record:read, medical:record:write, ...
PATIENT    -> READ         -> user:read, medical:record:read, ...
FAMILY     -> READ         -> user:read, medical:record:read, ...
```

通过`PermissionExpandUtil.expand(action)`将权限级别展开为具体权限列表。

### 建议的细粒度权限设计

如果需要更灵活的权限管理,建议:

1. **创建Permission表**存储所有可用权限
2. **修改RolePermission表**新增`permission_code`字段

```sql
-- 修改后的角色权限关联表
CREATE TABLE care_platform_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code INT NOT NULL COMMENT '角色代码',
    permission_code VARCHAR(100) COMMENT '权限代码: user:read, medical:record:write',
    create_time DATETIME
);
```

3. **配置示例数据**

```sql
-- 医生角色拥有的权限
INSERT INTO care_platform_role_permission (role_code, permission_code) VALUES
(1, 'user:read'),           -- DOCTOR可以读取用户信息
(1, 'user:write'),          -- DOCTOR可以修改用户信息
(1, 'medical:record:read'), -- DOCTOR可以读取病历
(1, 'medical:record:write'),-- DOCTOR可以编辑病历
(1, 'prescription:create'); -- DOCTOR可以开处方

-- 患者角色拥有的权限
INSERT INTO care_platform_role_permission (role_code, permission_code) VALUES
(2, 'user:read'),           -- PATIENT可以读取用户信息
(2, 'medical:record:read'), -- PATIENT可以读取自己的病历
(2, 'appointment:create');  -- PATIENT可以预约挂号

-- 家属角色拥有的权限
INSERT INTO care_platform_role_permission (role_code, permission_code) VALUES
(3, 'user:read'),           -- FAMILY可以读取用户信息
(3, 'medical:record:read'); -- FAMILY可以读取关联患者的病历
```

## 与注解的配合使用

### 1. @RequireRole注解

使用角色枚举名称:

```java
@GetMapping("/doctor/patients")
@RequireRole("DOCTOR")  // 需要DOCTOR角色
public Result getDoctorPatients() {
    // 医生查看患者列表
}

@GetMapping("/patient/records")
@RequireRole("PATIENT")  // 需要PATIENT角色
public Result getPatientRecords() {
    // 患者查看自己的病历
}
```

### 2. @RequirePermission注解

使用具体的权限码:

```java
@GetMapping("/users/{id}")
@RequirePermission("user:read")  // 需要user:read权限
public Result getUser(@PathVariable Long id) {
    // 读取用户信息
}

@PutMapping("/medical/record/{id}")
@RequirePermission("medical:record:write")  // 需要medical:record:write权限
public Result updateMedicalRecord(@PathVariable Long id, @RequestBody RecordDTO record) {
    // 编辑病历
}
```

## 数据流转过程

### 1. 用户登录

```java
// 1. 用户登录成功后,查询用户的角色
List<Role> roles = roleMapper.selectByUserId(userId);

// 2. 将角色code转换为角色名称
List<String> roleNames = roles.stream()
    .map(Role::getRoleCodeName)  // 获取"DOCTOR", "PATIENT"等
    .collect(Collectors.toList());

// 3. 查询角色对应的权限
Set<String> permissions = new HashSet<>();
for (Role role : roles) {
    List<RolePermission> rps = rolePermissionMapper.selectByRoleCode(role.getCode());
    for (RolePermission rp : rps) {
        // 方式1: 如果使用Action枚举
        Set<String> expanded = PermissionExpandUtil.expand(rp.getActionCode());
        permissions.addAll(expanded);
        
        // 方式2: 如果使用permission_code字段
        // permissions.add(rp.getPermissionCode());
    }
}

// 4. 生成JWT Token
GenTokenRequest request = new GenTokenRequest();
request.setUserId(userId);
request.setUsername(username);
request.setRoles(roleNames);  // ["DOCTOR", "PATIENT"]
request.setPermissions(permissions);  // ["user:read", "user:write", "medical:record:read", ...]

GenTokenResponse response = jwtAPI.genToken(request);
```

### 2. Gateway验证Token

```java
// Gateway解析JWT Token
ValidateTokenResponse response = tokenValidationAPI.validateToken(token);

// 获取用户信息
Long userId = response.getUserId();
List<String> roles = response.getRoles();  // ["DOCTOR"]
List<String> authorities = response.getAuthorities();  // ["user:read", "user:write", ...]

// 注入到请求头
request.header("X-User-Id", userId);
request.header("X-User-Roles", String.join(",", roles));  // "DOCTOR"
request.header("X-User-Authorities", String.join(",", authorities));  // "user:read,user:write,..."
```

### 3. 下游服务权限校验

```java
// 拦截器从请求头提取用户信息
String roles = request.getHeader("X-User-Roles");  // "DOCTOR"
String authorities = request.getHeader("X-User-Authorities");  // "user:read,user:write,..."

// AOP切面校验权限
@RequireRole("DOCTOR")  // 匹配成功: roles包含"DOCTOR"
@RequirePermission("user:read")  // 匹配成功: authorities包含"user:read"
```

## 权限命名规范

### 格式: `resource:operation[:target]`

```
user:read              - 读取用户信息
user:write             - 修改用户信息
user:delete            - 删除用户
user:create            - 创建用户

medical:record:read    - 读取病历
medical:record:write   - 编辑病历
medical:record:delete  - 删除病历

order:read             - 读取订单
order:create           - 创建订单
order:cancel           - 取消订单

prescription:create    - 开处方
prescription:review    - 审核处方

appointment:create     - 预约挂号
appointment:cancel     - 取消预约

data:export            - 数据导出
data:import            - 数据导入

system:config          - 系统配置
system:log             - 系统日志
```

## 初始化数据示例

```sql
-- 插入角色
INSERT INTO care_platform_role (role_id, code, name, description, action) VALUES
(1, 1, '医生', '医疗系统医生角色', 3),
(2, 2, '患者', '医疗系统患者角色', 1),
(3, 3, '家属', '患者家属角色', 1);

-- 插入角色权限关联(使用action_code)
INSERT INTO care_platform_role_permission (role_code, action_code) VALUES
(1, 3),  -- DOCTOR拥有ALL权限
(2, 1),  -- PATIENT拥有READ权限
(3, 1);  -- FAMILY拥有READ权限

-- 如果使用细粒度权限,插入权限定义
INSERT INTO care_platform_permission (permission_id, permission_code, name, category) VALUES
(1, 'user:read', '读取用户信息', 'USER'),
(2, 'user:write', '修改用户信息', 'USER'),
(3, 'medical:record:read', '读取病历', 'MEDICAL'),
(4, 'medical:record:write', '编辑病历', 'MEDICAL'),
(5, 'prescription:create', '开处方', 'MEDICAL'),
(6, 'appointment:create', '预约挂号', 'APPOINTMENT');

-- 配置医生角色的权限(使用permission_code)
INSERT INTO care_platform_role_permission (role_code, permission_code) VALUES
(1, 'user:read'),
(1, 'user:write'),
(1, 'medical:record:read'),
(1, 'medical:record:write'),
(1, 'prescription:create');
```

## 总结

### 当前系统设计

✅ 使用`code`字段存储角色枚举值(1, 2, 3)  
✅ 使用`actionCode`字段存储权限级别(1-READ, 2-WRITE, 3-ALL)  
✅ 通过`PermissionExpandUtil`展开权限  
✅ JWT Token存储角色名称(DOCTOR, PATIENT)和展开后的权限列表  
✅ 注解使用角色名称和权限码进行匹配  

### 建议的优化方向

如果需要更灵活的权限管理,建议:

1. 创建`Permission`表存储所有可用权限
2. 修改`RolePermission`表,新增`permission_code`字段
3. 直接存储和使用权限码,无需展开
4. 便于动态配置和管理权限

这种设计既保持了当前系统的兼容性,又为未来的扩展留下了空间。
