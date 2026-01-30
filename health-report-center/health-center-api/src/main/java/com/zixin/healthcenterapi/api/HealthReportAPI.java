package com.zixin.healthcenterapi.api;

import com.zixin.healthcenterapi.dto.*;

/**
 * 健康报告中心 Dubbo API
 * 
 * 提供健康报告的上传、查询等核心功能
 * 
 * @author zixin
 */
public interface HealthReportAPI {
    
    /**
     * 上传健康报告
     * 
     * 功能说明:
     * 1. 支持图片、PDF、文字三种类型报告
     * 2. 图片/PDF类型需要先通过OSS上传文件
     * 3. 自动关联患者的主治医生
     * 4. 报告默认状态为待审核
     * 
     * 权限要求:
     * - 患者本人可上传
     * - 主治医生可代为上传
     * 
     * @param request 上传请求
     * @return 上传响应(包含reportId和fileUrl)
     */
    UploadReportResponse uploadReport(UploadReportRequest request);
    
    /**
     * 查询健康报告列表
     * 
     * 功能说明:
     * 1. 支持按患者ID查询
     * 2. 支持按报告类型、分类、状态筛选
     * 3. 分页查询
     * 
     * 权限控制:
     * - 患者只能查看自己的报告
     * - 医生只能查看自己患者的报告
     * - 管理员可查看所有报告
     * 
     * @param request 查询请求
     * @return 查询响应(包含报告列表和分页信息)
     */
    QueryReportListResponse queryReportList(QueryReportListRequest request);
    
    /**
     * 获取报告详情
     * 
     * 功能说明:
     * 1. 根据reportId获取完整报告信息
     * 2. 包含患者和医生的基本信息
     * 
     * 权限控制:
     * - 患者只能查看自己的报告
     * - 医生只能查看自己患者的报告
     * 
     * @param request 获取详情请求
     * @return 报告详情响应
     */
    GetReportDetailResponse getReportDetail(GetReportDetailRequest request);
}
