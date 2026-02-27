package com.zixin.doctorconsumer.controller;

import com.zixin.doctorapi.api.ReportProcessingAPI;
import com.zixin.healthcenterapi.dto.ProcessReportRequest;
import com.zixin.healthcenterapi.dto.ProcessReportResponse;
import com.zixin.utils.exception.ToBCodeEnum;
import com.zixin.utils.security.RequireRole;
import com.zixin.utils.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/doctor/report")
@Slf4j
public class ReportProcessingController {
    @DubboReference(timeout = 50000)
    private ReportProcessingAPI reportProcessingAPI;

    @PostMapping("/process")
    @RequireRole("DOCTOR")
    public Result<?> processReport(@RequestBody ProcessReportRequest request) {
        log.info("Received request to process report: {}", request);
        ProcessReportResponse response = reportProcessingAPI.processReport(request);
        if (response.getCode().equals(ToBCodeEnum.SUCCESS)) {
            log.info("Successfully processed report with ID: {}", request.getReportId());
            return Result.success();
        } else {
            log.error("Failed to process report with ID: {}", request.getReportId());
            return Result.error();
        }
    }
}
