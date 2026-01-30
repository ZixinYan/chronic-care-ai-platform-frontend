package com.zixin.healthcenterapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 上传健康报告响应
 * 
 * @author zixin
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UploadReportResponse extends BaseResponse {
    
    /**
     * 报告ID
     */
    private Long reportId;
    
    /**
     * 文件URL (图片/PDF类型返回)
     */
    private String fileUrl;
}
