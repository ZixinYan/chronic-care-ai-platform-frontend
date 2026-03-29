package com.zixin.healthcenterapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 上传健康报告响应
 * 
 * @author zixin
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UploadReportResponse extends BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 报告ID
     */
    private Long reportId;
    
    /**
     * 文件URL (图片/PDF类型返回)
     */
    private String fileUrl;
}
