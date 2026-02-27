package com.zixin.doctorprovider.service;

import com.zixin.doctorapi.api.ReportProcessingAPI;
import com.zixin.doctorprovider.client.HealthReportClient;
import com.zixin.healthcenterapi.dto.ProcessReportRequest;
import com.zixin.healthcenterapi.dto.ProcessReportResponse;
import com.zixin.healthcenterapi.enums.ReportStatus;
import com.zixin.utils.exception.ToBCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

@Service
@DubboService
@Slf4j
public class ReportProcessingServiceImpl implements ReportProcessingAPI {

    private final HealthReportClient healthReportClient;

    public ReportProcessingServiceImpl(HealthReportClient healthReportClient) {
        this.healthReportClient = healthReportClient;
    }


    @Override
    public ProcessReportResponse processReport(ProcessReportRequest request) {
        ProcessReportResponse response = new ProcessReportResponse();
        // 1. 验证请求参数
        if (request.getReportId() == null || request.getResult() == null) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("Report ID and Result are required");
            log.error("Invalid request: reportId or result is null");
            return response;
        }
        if(request.getResult().equals(ReportStatus.PENDING.getCode())){
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("Result cannot be PENDING");
            log.error("Invalid request: result cannot be PENDING");
            return response;
        }

        // 2. 处理报告结果
        ProcessReportRequest processReportRequest = new ProcessReportRequest();
        processReportRequest.setReportId(request.getReportId());
        processReportRequest.setResult(request.getResult());
        processReportRequest.setComment(request.getComment());
        if(!healthReportClient.processHealthReport(request)){
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage(ToBCodeEnum.FAIL.getMessage());
            return response;
        }
        response.setCode(ToBCodeEnum.SUCCESS);
        response.setMessage("Report processed successfully");
        return response;
    }
}
