package com.zixin.healthcenterapi.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传健康报告请求
 * 
 * @author zixin
 */
@Data
public class UploadReportRequest {
    /**
     * 上传ID
     */
    private Long uploaderId;
    
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 报告类型 (1-图片, 2-文字, 3-PDF)
     */
    private Integer reportType;
    
    /**
     * 报告分类
     */
    private String category;
    
    /**
     * 报告标题
     */
    private String title;
    
    /**
     * 报告描述
     */
    private String description;
    
    /**
     * 上传的文件 (图片/PDF)
     * 仅当reportType为IMAGE或PDF时需要
     */
    private MultipartFile file;
    
    /**
     * 文字内容
     * 仅当reportType为TEXT时需要
     */
    private String textContent;
    
    /**
     * 报告日期 (格式: yyyy-MM-dd)
     */
    private String reportDate;
    
    /**
     * 医疗机构名称
     */
    private String hospitalName;
}
