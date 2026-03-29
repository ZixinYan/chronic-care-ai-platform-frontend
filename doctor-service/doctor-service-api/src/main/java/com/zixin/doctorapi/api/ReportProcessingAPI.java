package com.zixin.doctorapi.api;


import com.zixin.healthcenterapi.dto.ProcessReportRequest;
import com.zixin.healthcenterapi.dto.ProcessReportResponse;

public interface ReportProcessingAPI {
        /**
        * 处理健康报告
        *
        * 功能说明:
        * 1. 医生对患者上传的健康报告进行审核和处理
        * 2. 可以添加处理意见和建议 auditMark
        * 3. 更新报告状态（如：待处理、已处理、驳回等） status
        *
        * 权限控制:
        * - 只有主治医生可以处理对应患者的报告
        *
        * @param request 处理请求
        * @return 处理响应(包含处理结果和更新后的报告状态)
        */
        ProcessReportResponse processReport(ProcessReportRequest request);
}
