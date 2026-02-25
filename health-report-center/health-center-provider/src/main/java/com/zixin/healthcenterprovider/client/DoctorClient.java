package com.zixin.healthcenterprovider.client;

import com.zixin.doctorapi.api.DoctorWorkbenchAPI;
import com.zixin.doctorapi.dto.AddScheduleRequest;
import com.zixin.doctorapi.dto.AddScheduleResponse;
import com.zixin.doctorapi.vo.ScheduleVO;
import com.zixin.utils.context.UserInfoManager;
import com.zixin.utils.exception.ToBCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.PathMatcher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class DoctorClient {

    @DubboReference(timeout = 50000)
    private DoctorWorkbenchAPI doctorWorkbenchAPI;

    private final ExecutorService scheduleExecutor = Executors.newFixedThreadPool(10);



    public CompletableFuture<Boolean> addScheduleAsync(Long doctorId, Long patientId, String doctorName, ScheduleVO scheduleVO) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("开始异步添加排班, doctorId: {}, doctorName: {}, scheduleDate: {}",
                        doctorId, doctorName, scheduleVO.getScheduleDay());

                AddScheduleRequest request = new AddScheduleRequest();
                request.setDoctorId(doctorId);
                request.setDoctorName(doctorName);
                request.setSchedule(scheduleVO);
                request.setPatientId(patientId);
                AddScheduleResponse response = doctorWorkbenchAPI.addSchedule(request);

                if (response.getCode().equals(ToBCodeEnum.SUCCESS)) {
                    log.info("异步添加排班成功, doctorId: {}, message: {}",
                            doctorId, response.getMessage());
                    return true;
                } else {
                    log.error("异步添加排班失败, doctorId: {}, error: {}",
                            doctorId, response.getMessage());
                    return false;
                }
            } catch (Exception e) {
                log.error("异步添加排班异常, doctorId: {}", doctorId, e);
                throw new RuntimeException("排班添加异常", e);
            }
        }, scheduleExecutor);
    }

    /**
     * 同步添加排班
     * @param doctorId 医生ID
     * @param patientId 患者ID
     * @param doctorName 医生姓名
     * @param scheduleVO 排班信息
     * @return true-成功 false-失败
     */
    public boolean addSchedule(Long doctorId, Long patientId, String doctorName, ScheduleVO scheduleVO) {
        try {
            log.debug("开始添加排班, doctorId: {}, doctorName: {}, scheduleDate: {}",
                    doctorId, doctorName, scheduleVO.getScheduleDay());

            AddScheduleRequest request = new AddScheduleRequest();
            request.setDoctorId(doctorId);
            request.setDoctorName(doctorName);
            request.setSchedule(scheduleVO);
            request.setPatientId(patientId);

            AddScheduleResponse response = doctorWorkbenchAPI.addSchedule(request);

            if (response.getCode().equals(ToBCodeEnum.SUCCESS)) {
                log.info("添加排班成功, doctorId: {}, message: {}",
                        doctorId, response.getMessage());
                return true;
            } else {
                log.error("添加排班失败, doctorId: {}, error: {}",
                        doctorId, response.getMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("添加排班异常, doctorId: {}", doctorId, e);
            throw new RuntimeException("排班添加异常", e);
        }
    }

}
