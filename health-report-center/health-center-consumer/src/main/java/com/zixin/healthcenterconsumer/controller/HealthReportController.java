package com.zixin.healthcenterconsumer.controller;

import com.zixin.healthcenterapi.api.HealthReportAPI;
import com.zixin.healthcenterapi.dto.*;
import com.zixin.utils.context.UserInfoManager;
import com.zixin.utils.exception.BusinessException;
import com.zixin.utils.exception.ToBCodeEnum;
import com.zixin.utils.security.RequirePermission;
import com.zixin.utils.security.RequireRole;
import com.zixin.utils.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * 健康报告控制器
 * 
 * 提供健康报告的上传、查询等HTTP接口
 * 
 * 权限要求:
 * - 患者可上传和查看自己的报告
 * - 医生可查看自己患者的报告
 * 
 * @author zixin
 */
@RestController
@RequestMapping("/health/report")
@Slf4j
public class HealthReportController {
    
    @DubboReference(version = "1.0.0", check = false)
    private HealthReportAPI healthReportAPI;
    
    /**
     * 允许的图片文件类型
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/png", "image/jpeg", "image/jpg"
    );
    
    /**
     * 允许的PDF文件类型
     */
    private static final List<String> ALLOWED_PDF_TYPES = Arrays.asList(
            "application/pdf"
    );
    
    /**
     * 文件大小限制 (10MB)
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /**
     * 上传健康报告
     * 
     * 支持三种类型:
     * 1. 图片报告 (reportType=1, 需要file参数)
     * 2. 文字报告 (reportType=2, 需要textContent参数)
     * 3. PDF报告 (reportType=3, 需要file参数)
     * 
     * 权限要求:
     * - 需要PATIENT角色
     * - 需要health:report:write权限
     * - 只能上传自己的报告
     * 
     * @param patientId 患者ID
     * @param reportType 报告类型 (1-图片, 2-文字, 3-PDF)
     * @param category 报告分类
     * @param title 报告标题
     * @param description 报告描述
     * @param file 报告文件 (图片或PDF)
     * @param textContent 文字内容
     * @param reportDate 报告日期 (格式: yyyy-MM-dd)
     * @param hospitalName 医疗机构名称
     * @param request HTTP请求对象
     * @return 上传结果
     */
    @PostMapping("/upload")
    @RequireRole("PATIENT")
    public Result<UploadReportResponse> uploadReport(
            @RequestParam Long patientId,
            @RequestParam Integer reportType,
            @RequestParam(required = false) String category,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String textContent,
            @RequestParam(required = false) String reportDate,
            @RequestParam(required = false) String hospitalName,
            HttpServletRequest request) {
        
        // 从ThreadLocal获取当前用户信息
        Long currentUserId = UserInfoManager.getUserIdOrThrow();
        String traceId = UserInfoManager.getTraceId();
        String clientIp = UserInfoManager.getRequestIp();
        
        log.info("uploadReport - userId: {}, patientId: {}, reportType: {}, title: {}, ip: {}, traceId: {}", 
                currentUserId, patientId, reportType, title, clientIp, traceId);
        
        // 1. 权限校验: 患者只能上传自己的报告
        if (!patientId.equals(currentUserId)) {
            log.warn("uploadReport - 权限拒绝: userId {} 尝试上传 patientId {} 的报告", currentUserId, patientId);
            throw new BusinessException("无权上传他人报告");
        }
        
        // 2. 文件安全校验
        if (file != null && !file.isEmpty()) {
            validateFile(file, reportType);
        }
        
        // 3. 构建请求
        UploadReportRequest uploadRequest = new UploadReportRequest();
        uploadRequest.setUploaderId(currentUserId);  // 设置上传者ID
        uploadRequest.setPatientId(patientId);
        uploadRequest.setReportType(reportType);
        uploadRequest.setCategory(category);
        uploadRequest.setTitle(title);
        uploadRequest.setDescription(description);
        uploadRequest.setFile(file);
        uploadRequest.setTextContent(textContent);
        uploadRequest.setReportDate(reportDate);
        uploadRequest.setHospitalName(hospitalName);
        
        // 4. 调用Dubbo服务
        UploadReportResponse response = healthReportAPI.uploadReport(uploadRequest);
        
        if (ToBCodeEnum.SUCCESS.equals(response.getCode())) {
            return Result.success(response);
        } else {
            throw new BusinessException(response.getMessage());
        }
    }
    
    /**
     * 查询健康报告列表
     * 
     * 权限要求:
     * - 需要PATIENT角色
     * - 需要health:report:read权限
     * - 患者只能查看自己的报告
     * 
     * @param patientId 患者ID (必填)
     * @param reportType 报告类型 (可选)
     * @param category 报告分类 (可选)
     * @param status 审核状态 (可选)
     * @param pageNum 页码 (默认1)
     * @param pageSize 每页数量 (默认10)
     * @return 报告列表
     */
    @GetMapping("/list")
    @RequireRole("PATIENT")
    public Result<QueryReportListResponse> queryReportList(
            @RequestParam Long patientId,
            @RequestParam(required = false) Integer reportType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // 从ThreadLocal获取当前用户信息
        Long currentUserId = UserInfoManager.getUserIdOrThrow();
        String traceId = UserInfoManager.getTraceId();
        
        log.info("queryReportList - userId: {}, patientId: {}, pageNum: {}, pageSize: {}, traceId: {}", 
                currentUserId, patientId, pageNum, pageSize, traceId);
        
        // 权限校验: 患者只能查看自己的报告
        if (!patientId.equals(currentUserId)) {
            log.warn("queryReportList - 权限拒绝: userId {} 尝试查看 patientId {} 的报告", currentUserId, patientId);
            throw new BusinessException("无权查看他人报告");
        }
        
        // 构建请求
        QueryReportListRequest queryRequest = new QueryReportListRequest();
        queryRequest.setPatientId(patientId);
        queryRequest.setReportType(reportType);
        queryRequest.setCategory(category);
        queryRequest.setStatus(status);
        queryRequest.setPageNum(pageNum);
        queryRequest.setPageSize(pageSize);
        
        // 调用Dubbo服务
        QueryReportListResponse response = healthReportAPI.queryReportList(queryRequest);
        
        if (ToBCodeEnum.SUCCESS.equals(response.getCode())) {
            return Result.success(response);
        } else {
            throw new BusinessException(response.getMessage());
        }
    }
    
    /**
     * 获取报告详情
     * 
     * 权限要求:
     * - 需要PATIENT角色
     * - 只能查看自己的报告详情
     * 
     * @param reportId 报告ID
     * @return 报告详情
     */
    @GetMapping("/detail")
    @RequireRole("PATIENT")
    public Result<GetReportDetailResponse> getReportDetail(@RequestParam Long reportId) {
        // 从ThreadLocal获取当前用户信息
        Long currentUserId = UserInfoManager.getUserIdOrThrow();
        String traceId = UserInfoManager.getTraceId();
        
        log.info("getReportDetail - userId: {}, reportId: {}, traceId: {}", 
                currentUserId, reportId, traceId);
        
        // 构建请求
        GetReportDetailRequest detailRequest = new GetReportDetailRequest();
        detailRequest.setReportId(reportId);
        
        // 调用Dubbo服务
        GetReportDetailResponse response = healthReportAPI.getReportDetail(detailRequest);
        
        if ("SUCCESS".equals(response.getCode().name())) {
            return Result.success(response);
        } else {
            throw new BusinessException(response.getMessage());
        }
    }
    
    /**
     * 文件安全校验
     * 
     * 校验内容:
     * 1. 文件大小不超过10MB
     * 2. 文件类型必须在白名单中
     * 3. 文件后缀合法
     * 
     * @param file 上传的文件
     * @param reportType 报告类型
     * @throws BusinessException 校验失败抛出异常
     */
    private void validateFile(MultipartFile file, Integer reportType) {
        // 1. 文件大小校验 (10MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            log.error("validateFile - 文件过大: {} bytes, 限制: {} bytes", file.getSize(), MAX_FILE_SIZE);
            throw new BusinessException("文件大小不能超过10MB");
        }
        
        // 2. 文件类型校验
        String contentType = file.getContentType();
        if (contentType == null) {
            log.error("validateFile - 文件类型为空");
            throw new BusinessException("无法识别文件类型");
        }
        
        // 根据reportType校验文件类型
        if (reportType == 1) {  // 图片类型
            if (!ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                log.error("validateFile - 不支持的图片类型: {}", contentType);
                throw new BusinessException("只支持PNG、JPG、JPEG格式的图片");
            }
        } else if (reportType == 3) {  // PDF类型
            if (!ALLOWED_PDF_TYPES.contains(contentType.toLowerCase())) {
                log.error("validateFile - 不支持的PDF类型: {}", contentType);
                throw new BusinessException("只支持PDF格式的文档");
            }
        }
        
        // 3. 文件后缀校验
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String lowerFilename = filename.toLowerCase();
            
            if (reportType == 1) {  // 图片类型
                if (!lowerFilename.matches(".*\\.(png|jpg|jpeg)$")) {
                    log.error("validateFile - 图片文件后缀不合法: {}", filename);
                    throw new BusinessException("图片文件后缀必须是png、jpg或jpeg");
                }
            } else if (reportType == 3) {  // PDF类型
                if (!lowerFilename.endsWith(".pdf")) {
                    log.error("validateFile - PDF文件后缀不合法: {}", filename);
                    throw new BusinessException("PDF文件后缀必须是.pdf");
                }
            }
        }
        
        log.info("validateFile - 文件校验通过: filename={}, contentType={}, size={} bytes", 
                filename, contentType, file.getSize());
    }
}
