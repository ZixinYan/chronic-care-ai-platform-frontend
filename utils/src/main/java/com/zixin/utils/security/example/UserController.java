//package com.zixin.utils.security.example;
//
//import com.zixin.utils.security.RequirePermission;
//import com.zixin.utils.security.RequireRole;
//import com.zixin.utils.security.UserInfoInterceptor;
//import com.zixin.utils.utils.Result;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 用户控制器示例
// * 演示如何使用权限和角色注解进行接口级别的权限控制
// */
//@Slf4j
//@RestController
//@RequestMapping("/api/user")
//public class UserController {
//
//    /**
//     * 获取用户信息
//     * 需要有 user:read 权限
//     */
//    @GetMapping("/profile")
//    @RequirePermission("user:read")
//    public Result getUserProfile() {
//        Long userId = UserInfoInterceptor.getCurrentUserId();
//        String roles = UserInfoInterceptor.getCurrentUserRoles();
//        String authorities = UserInfoInterceptor.getCurrentUserAuthorities();
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("userId", userId);
//        data.put("roles", roles);
//        data.put("authorities", authorities);
//        data.put("message", "User profile retrieved successfully");
//
//        log.info("User {} retrieved profile", userId);
//        return Result.success(data);
//    }
//
//    /**
//     * 更新用户信息
//     * 需要同时拥有 user:read 和 user:write 权限
//     */
//    @PutMapping("/profile")
//    @RequirePermission(value = {"user:read", "user:write"}, logical = RequirePermission.Logical.AND)
//    public Result updateUserProfile(@RequestBody Map<String, Object> updates) {
//        Long userId = UserInfoInterceptor.getCurrentUserId();
//
//        log.info("User {} updating profile with data: {}", userId, updates);
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("userId", userId);
//        data.put("updates", updates);
//        data.put("message", "Profile updated successfully");
//
//        return Result.success(data);
//    }
//
//    /**
//     * 删除用户
//     * 需要 user:delete 权限
//     */
//    @DeleteMapping("/{userId}")
//    @RequirePermission("user:delete")
//    public Result deleteUser(@PathVariable Long userId) {
//        Long currentUserId = UserInfoInterceptor.getCurrentUserId();
//
//        log.warn("User {} attempting to delete user {}", currentUserId, userId);
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("deletedUserId", userId);
//        data.put("operatorUserId", currentUserId);
//        data.put("message", "User deleted successfully");
//
//        return Result.success(data);
//    }
//
//    /**
//     * 管理员专属接口
//     * 需要拥有 ADMIN 角色
//     */
//    @GetMapping("/admin/dashboard")
//    @RequireRole("ADMIN")
//    public Result getAdminDashboard() {
//        Long userId = UserInfoInterceptor.getCurrentUserId();
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("userId", userId);
//        data.put("totalUsers", 1000);
//        data.put("activeUsers", 856);
//        data.put("message", "Admin dashboard data");
//
//        log.info("Admin {} accessed dashboard", userId);
//        return Result.success(data);
//    }
//
//    /**
//     * 管理员或经理可访问
//     * 需要拥有 ADMIN 或 MANAGER 角色之一
//     */
//    @GetMapping("/reports")
//    @RequireRole(value = {"ADMIN", "MANAGER"}, logical = RequireRole.Logical.OR)
//    public Result getReports() {
//        Long userId = UserInfoInterceptor.getCurrentUserId();
//        String roles = UserInfoInterceptor.getCurrentUserRoles();
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("userId", userId);
//        data.put("userRole", roles);
//        data.put("reports", "Monthly reports data");
//
//        log.info("User {} with roles {} accessed reports", userId, roles);
//        return Result.success(data);
//    }
//
//    /**
//     * 超级管理员专属
//     * 需要同时拥有 ADMIN 和 SUPER 角色
//     */
//    @PostMapping("/system/settings")
//    @RequireRole(value = {"ADMIN", "SUPER"}, logical = RequireRole.Logical.AND)
//    public Result updateSystemSettings(@RequestBody Map<String, Object> settings) {
//        Long userId = UserInfoInterceptor.getCurrentUserId();
//
//        log.warn("User {} updating system settings: {}", userId, settings);
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("userId", userId);
//        data.put("settings", settings);
//        data.put("message", "System settings updated");
//
//        return Result.success(data);
//    }
//
//    /**
//     * 复杂权限校验示例
//     * 需要同时拥有用户读写权限和数据导出权限
//     */
//    @GetMapping("/export")
//    @RequirePermission(value = {"user:read", "user:write", "data:export"}, logical = RequirePermission.Logical.AND)
//    public Result exportUserData() {
//        Long userId = UserInfoInterceptor.getCurrentUserId();
//
//        log.info("User {} exporting data", userId);
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("userId", userId);
//        data.put("exportUrl", "https://example.com/export/users.csv");
//        data.put("message", "Export initiated");
//
//        return Result.success(data);
//    }
//
//    /**
//     * 公开接口示例
//     * 不需要任何权限，但会从Gateway获取用户信息(如果有登录)
//     */
//    @GetMapping("/public/info")
//    public Result getPublicInfo() {
//        Long userId = UserInfoInterceptor.getCurrentUserId();
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("loggedIn", userId != null);
//        if (userId != null) {
//            data.put("userId", userId);
//        }
//        data.put("message", "This is public information");
//
//        return Result.success(data);
//    }
//
//    /**
//     * 医生专属接口
//     * 需要拥有 DOCTOR 角色
//     */
//    @GetMapping("/doctor/patients")
//    @RequireRole("DOCTOR")
//    public Result getDoctorPatients() {
//        Long userId = UserInfoInterceptor.getCurrentUserId();
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("doctorId", userId);
//        data.put("patients", "List of patients");
//        data.put("message", "Doctor patients retrieved");
//
//        log.info("Doctor {} accessed patient list", userId);
//        return Result.success(data);
//    }
//
//    /**
//     * 患者专属接口
//     * 需要拥有 PATIENT 角色
//     */
//    @GetMapping("/patient/records")
//    @RequireRole("PATIENT")
//    public Result getPatientRecords() {
//        Long userId = UserInfoInterceptor.getCurrentUserId();
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("patientId", userId);
//        data.put("records", "Medical records");
//        data.put("message", "Patient records retrieved");
//
//        log.info("Patient {} accessed medical records", userId);
//        return Result.success(data);
//    }
//}
