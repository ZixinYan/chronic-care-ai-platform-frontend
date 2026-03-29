package com.zixin.doctorprovider.client;

import com.zixin.accountapi.api.UserIdentityAPI;
import com.zixin.healthcenterapi.api.HealthReportAPI;
import com.zixin.healthcenterapi.dto.ProcessReportRequest;
import com.zixin.healthcenterapi.dto.ProcessReportResponse;
import com.zixin.utils.exception.ToBCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HealthReportClient {
    @DubboReference(timeout = 50000)
    private HealthReportAPI healthReportAPI;

    public boolean processHealthReport(ProcessReportRequest request) {
        ProcessReportResponse response = healthReportAPI.processReport(request);
        if (!response.getCode().equals(ToBCodeEnum.SUCCESS)) {
            log.error("Failed to process health report");
            throw new RuntimeException("Failed to process health report: " + response.getMessage());
        }
        log.info("Successfully processed health report, reportId: {}", request.getReportId());
        return true;
    }
}
