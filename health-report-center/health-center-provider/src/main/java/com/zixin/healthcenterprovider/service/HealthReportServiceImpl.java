package com.zixin.healthcenterprovider.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zixin.accountapi.vo.DoctorVO;
import com.zixin.accountapi.vo.PatientVO;
import com.zixin.aicapabilityapi.dto.GenerateScheduleRequest;
import com.zixin.aicapabilityapi.dto.GenerateScheduleResponse;
import com.zixin.doctorapi.enums.ScheduleCategory;
import com.zixin.doctorapi.enums.SchedulePriority;
import com.zixin.doctorapi.enums.ScheduleStatus;
import com.zixin.doctorapi.vo.ScheduleVO;
import com.zixin.healthcenterapi.api.HealthReportAPI;
import com.zixin.healthcenterapi.dto.*;
import com.zixin.healthcenterapi.enums.ReportStatus;
import com.zixin.healthcenterapi.enums.ReportType;
import com.zixin.healthcenterapi.po.HealthReport;
import com.zixin.healthcenterapi.vo.HealthReportVO;
import com.zixin.healthcenterprovider.client.AiClient;
import com.zixin.healthcenterprovider.client.DoctorClient;
import com.zixin.healthcenterprovider.client.MessageClient;
import com.zixin.healthcenterprovider.client.UserIdentityClient;
import com.zixin.healthcenterprovider.mapper.HealthReportMapper;
import com.zixin.messageapi.dto.SendMessageRequest;
import com.zixin.messageapi.enums.MessageType;
import com.zixin.utils.context.UserInfoManager;
import com.zixin.utils.exception.ToBCodeEnum;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.zixin.utils.constant.BizCode.HEALTHY_REPORT_JUDGEMENT;

/**
 * 健康报告服务实现
 * 
 * 核心功能:
 * 1. 报告上传(支持图片、PDF、文字)
 * 2. 报告查询(列表和详情)
 * 3. 权限控制(患者和医生)
 * 
 * @author zixin
 */
@Slf4j
@Service
@DubboService(timeout = 30000)
public class HealthReportServiceImpl implements HealthReportAPI {
    
    private final HealthReportMapper healthReportMapper;
    private final MessageClient messageClient;
    private final UserIdentityClient userIdentityClient;
    private final DoctorClient doctorClient;
    private final TransactionTemplate transactionTemplate;
    private final AiClient aiClient;
    
    public HealthReportServiceImpl(HealthReportMapper healthReportMapper,
                                   MessageClient messageClient,
                                   UserIdentityClient userIdentityClient, DoctorClient doctorClient, TransactionTemplate transactionTemplate, AiClient aiClient) {
        this.healthReportMapper = healthReportMapper;
        this.messageClient = messageClient;
        this.userIdentityClient = userIdentityClient;
        this.doctorClient = doctorClient;
        this.transactionTemplate = transactionTemplate;
        this.aiClient = aiClient;
    }

    @Override
    public UploadReportResponse uploadReport(UploadReportRequest request) {
        StopWatch sw = new StopWatch("uploadReport");
        sw.start("参数校验");

        UploadReportResponse response = new UploadReportResponse();

        try {
            // 1. 参数校验
            if (request.getPatientId() == null) {
                // 如果没有传patientId，默认使用当前登录用户ID
                request.setPatientId(UserInfoManager.getUserId());
            }

            final PatientVO patient = userIdentityClient.getPatientInfo(request.getPatientId());
            if (patient == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("患者不存在");
                return response;
            }

            if (request.getReportType() == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("报告类型不能为空");
                return response;
            }

            ReportType reportType = ReportType.fromCode(request.getReportType());
            if (reportType == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("无效的报告类型");
                return response;
            }

            sw.stop();
            log.info("uploadReport - 参数校验完成, patientId: {}, reportType: {}",
                    request.getPatientId(), reportType.getDescription());

            // 2. 获取患者信息
            if (patient == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("患者信息查询失败");
                return response;
            }

            // 采用AI判断给哪个医生进行检查
            GenerateScheduleRequest generateScheduleRequest = new GenerateScheduleRequest();
            generateScheduleRequest.setScheduleDay(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            generateScheduleRequest.setBusinessRequirement(HEALTHY_REPORT_JUDGEMENT.getBizName() + "，患者：" + patient + "，报告：" + request.getDescription());
            GenerateScheduleResponse suggestSchedule = aiClient.generateSchedule(generateScheduleRequest);

            if (!ToBCodeEnum.SUCCESS.equals(suggestSchedule.getCode())
                    || suggestSchedule.getRecommendedSchedules() == null
                    || suggestSchedule.getRecommendedSchedules().isEmpty()
                    || suggestSchedule.getRecommendedSchedules().get(0).getDoctorId() == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage(suggestSchedule.getMessage() != null && !suggestSchedule.getMessage().isEmpty()
                        ? suggestSchedule.getMessage()
                        : "AI 排班未返回有效医生，请稍后重试");
                return response;
            }

            final DoctorVO doctor = userIdentityClient.getDoctorInfo(suggestSchedule.getRecommendedSchedules().get(0).getDoctorId());
            if (doctor == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("医生信息查询失败");
                return response;
            }

            // 3. 处理文件上传(图片或PDF类型)
            final String fileUrl = request.getFileUrl();

            // 4. 文字类型报告校验
            if (reportType == ReportType.TEXT) {
                if (request.getTextContent() == null || request.getTextContent().trim().isEmpty()) {
                    response.setCode(ToBCodeEnum.FAIL);
                    response.setMessage("文字报告内容不能为空");
                    return response;
                }
            }

            // 5. 构建排班VO
            ScheduleVO scheduleVO = buildScheduleVO(
                    patient,
                    doctor,
                    ScheduleStatus.PENDING,
                    SchedulePriority.HIGH,
                    ScheduleCategory.ONLINE_APPROVAL.getCode(),
                    ScheduleCategory.ONLINE_APPROVAL.getName(),
                    fileUrl
            );

            // 供事务内部使用的final变量

            // 6. 使用事务管理数据库操作和排班添加
            Boolean transactionResult = transactionTemplate.execute(status -> {
                try {
                    // 6.1 构建报告实体
                    HealthReport report = new HealthReport();
                    report.setPatientId(request.getPatientId());
                    report.setAttendingDoctorId(patient.getAttendingDoctorId());
                    report.setReportType(request.getReportType());
                    report.setCategory(request.getCategory());
                    report.setTitle(request.getTitle());
                    report.setDescription(request.getDescription());
                    report.setFileUrl(fileUrl);
                    report.setTextContent(request.getTextContent());
                    report.setReportDate(request.getReportDate());
                    report.setUploaderId(request.getUploaderId());
                    report.setUploaderName(patient.getNickname());
                    report.setHospitalName(request.getHospitalName());
                    report.setStatus(ReportStatus.PENDING.getCode());

                    // 6.2 保存到数据库
                    int rows = healthReportMapper.insert(report);

                    if (rows <= 0) {
                        log.error("uploadReport - 报告保存失败, patientId: {}", request.getPatientId());
                        status.setRollbackOnly();
                        response.setCode(ToBCodeEnum.FAIL);
                        response.setMessage("报告保存失败");
                        return false;
                    }

                    // 6.3 调用AI能力判断同步添加排班
                    if (patient.getAttendingDoctorId() != null) {
                        boolean scheduleSuccess = doctorClient.addSchedule(
                                doctor.getUserId(),
                                patient.getUserId(),
                                doctor.getUsername(),
                                scheduleVO
                        );

                        if (!scheduleSuccess) {
                            log.error("uploadReport - 添加排班失败, doctorId: {}", patient.getAttendingDoctorId());
                            status.setRollbackOnly();
                            response.setCode(ToBCodeEnum.FAIL);
                            response.setMessage("添加排班失败");
                            return false;
                        }

                        log.info("uploadReport - 排班添加成功, doctorId: {}", patient.getAttendingDoctorId());
                    }

                    // 6.4 设置成功响应
                    response.setCode(ToBCodeEnum.SUCCESS);
                    response.setMessage("报告上传成功");
                    response.setReportId(report.getReportId());
                    response.setFileUrl(fileUrl);

                    return true;

                } catch (Exception e) {
                    log.error("uploadReport - 事务执行异常", e);
                    status.setRollbackOnly();
                    response.setCode(ToBCodeEnum.FAIL);
                    response.setMessage("事务执行异常: " + e.getMessage());
                    return false;
                }
            });

            // 7. 事务成功后，异步发送消息（非事务性，失败不影响主流程）
            if (Boolean.TRUE.equals(transactionResult) && patient.getAttendingDoctorId() != null) {
                    // 异步发送消息
                try {
                    messageClient.sendMessageAsync(
                            patient.getUserId(),
                            SendMessageRequest.builder()
                                    .receiverId(patient.getAttendingDoctorId())
                                    .messageType(MessageType.SYSTEM.getCode())
                                    .title("新健康报告上传通知")
                                    .senderName(UserInfoManager.getUsername())
                                    .content("患者 " + patient.getUsername() + " 上传了新的健康报告，请及时查看。")
                                    .build()
                    );
                    log.info("uploadReport - 消息发送成功, doctorId: {}", patient.getAttendingDoctorId());
                } catch (Exception e) {
                    // 只记录日志，不影响主流程
                    log.error("uploadReport - 消息发送失败, doctorId: {}, error: {}",
                            patient.getAttendingDoctorId(), e.getMessage(), e);
                }
            }

            log.info("uploadReport - 报告上传完成, reportId: {}, patientId: {}, 耗时统计:\n{}",
                    response.getReportId(), request.getPatientId(), sw.prettyPrint());

        } catch (Exception e) {
            log.error("uploadReport - 报告上传异常, patientId: {}, error: {}",
                    request.getPatientId(), e.getMessage(), e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("报告上传异常: " + e.getMessage());
        }

        return response;
    }
    
    @Override
    public QueryReportListResponse queryReportList(QueryReportListRequest request) {
        StopWatch sw = new StopWatch("queryReportList");
        sw.start("构建查询条件");
        
        QueryReportListResponse response = new QueryReportListResponse();
        
        try {
            // 1. 参数校验
            if (request.getPatientId() == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("患者ID不能为空");
                return response;
            }
            
            // 2. 构建查询条件
            LambdaQueryWrapper<HealthReport> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HealthReport::getPatientId, request.getPatientId());
            
            if (request.getReportType() != null) {
                wrapper.eq(HealthReport::getReportType, request.getReportType());
            }
            
            if (request.getCategory() != null && !request.getCategory().isEmpty()) {
                wrapper.eq(HealthReport::getCategory, request.getCategory());
            }
            
            if (request.getStatus() != null) {
                wrapper.eq(HealthReport::getStatus, request.getStatus());
            }
            
            wrapper.orderByDesc(HealthReport::getCreateTime);
            
            sw.stop();
            
            // 3. 分页查询
            sw.start("执行分页查询");
            Page<HealthReport> page = new Page<>(request.getPageNum(), request.getPageSize());
            Page<HealthReport> resultPage = healthReportMapper.selectPage(page, wrapper);
            sw.stop();
            
            log.info("queryReportList - 查询完成, patientId: {}, total: {}", 
                    request.getPatientId(), resultPage.getTotal());
            
            // 4. 转换为VO
            sw.start("数据转换");
            List<HealthReportVO> voList = new ArrayList<>();
            for (HealthReport report : resultPage.getRecords()) {
                HealthReportVO vo = convertToVO(report);
                voList.add(vo);
            }
            sw.stop();
            
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("查询成功");
            response.setReportList(voList);
            response.setTotal(resultPage.getTotal());
            response.setPageNum(request.getPageNum());
            response.setPageSize(request.getPageSize());
            
            log.info("queryReportList - 查询成功, 耗时统计: {}", sw.prettyPrint());
            
        } catch (Exception e) {
            log.error("queryReportList - 查询异常, patientId: {}, error: {}", 
                    request.getPatientId(), e.getMessage(), e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("查询异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public GetReportDetailResponse getReportDetail(GetReportDetailRequest request) {
        GetReportDetailResponse response = new GetReportDetailResponse();
        
        try {
            // 1. 参数校验
            if (request.getReportId() == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("报告ID不能为空");
                return response;
            }
            
            // 2. 查询报告
            HealthReport report = healthReportMapper.selectById(request.getReportId());
            
            if (report == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("报告不存在");
                return response;
            }

            // 3. 权限校验: 只能查看自己的报告
            Long currentUserId = UserInfoManager.getUserIdOrThrow();
            if (!report.getPatientId().equals(currentUserId)) {
                log.warn("getReportDetail - 权限拒绝: userId {} 尝试查看 patientId {} 的报告 {}", 
                        currentUserId, report.getPatientId(), request.getReportId());
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("无权查看该报告");
                return response;
            }
            
            // 4. 转换为VO
            HealthReportVO vo = convertToVO(report);
            
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("查询成功");
            response.setReport(vo);
            
            log.info("getReportDetail - 查询成功, reportId: {}, patientId: {}", 
                    request.getReportId(), report.getPatientId());
            
        } catch (Exception e) {
            log.error("getReportDetail - 查询异常, reportId: {}, error: {}", 
                    request.getReportId(), e.getMessage(), e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("查询异常: " + e.getMessage());
        }
        
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessReportResponse processReport(ProcessReportRequest request) {
        ProcessReportResponse response = new ProcessReportResponse();
        log.info("processReport - 处理报告请求, reportId: {}, result: {}, auditMark: {}",
                request.getReportId(), request.getResult(), request.getComment());
        // 1. 获取报告详情
        HealthReport healthReport = healthReportMapper.selectById(request.getReportId());
        if (healthReport == null) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("报告不存在");
            return response;
        }
        int version = healthReport.getVersion();
        if(!Objects.equals(healthReport.getStatus(), ReportStatus.PENDING.getCode())){
            log.warn("processReport - 报告状态异常, reportId: {}, currentStatus: {}, expectedStatus: {}",
                    request.getReportId(), healthReport.getStatus(), ReportStatus.PENDING.getCode());
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("报告已被处理");
            return response;
        }
        // 2. 更新报告状态
        int status = ReportStatus.fromCode(request.getResult()).getCode();
        healthReport.setStatus(status);
        healthReport.setAuditRemark(request.getComment());
        // 3. 乐观锁更新
        int rows = healthReportMapper.updateById(healthReport);
        if (rows <= 0) {
            log.warn("processReport - 更新失败, reportId: {}, version: {}", request.getReportId(), version);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("处理失败");
            return response;
        }
        response.setCode(ToBCodeEnum.SUCCESS);
        response.setMessage("处理成功");
        // 4. 处理完成后，发送消息通知患者
        try {
            messageClient.sendMessageAsync(
                    UserInfoManager.getUserId(),
                    SendMessageRequest.builder()
                            .receiverId(healthReport.getPatientId())
                            .messageType(MessageType.SYSTEM.getCode())
                            .title("健康报告处理结果通知")
                            .senderName(UserInfoManager.getUsername())
                            .content("您的健康报告 '" + healthReport.getTitle() + "' 已经被处理，处理结果: "
                                    + ReportStatus.fromCode(request.getResult()).getDescription()
                                    + (request.getComment() != null ? "，备注: " + request.getComment() : ""))
                            .build()
            );
            log.info("processReport - 消息发送成功, patientId: {}", healthReport.getPatientId());
        } catch (Exception e) {
            log.error("processReport - 消息发送失败, patientId: {}, error: {}",
                    healthReport.getPatientId(), e.getMessage(), e);
        }
        return response;
    }

    /**
     * 将实体转换为VO
     */
    private HealthReportVO convertToVO(HealthReport report) {
        HealthReportVO vo = new HealthReportVO();
        BeanUtils.copyProperties(report, vo);
        
        // 设置报告类型描述
        ReportType reportType = ReportType.fromCode(report.getReportType());
        if (reportType != null) {
            vo.setReportTypeDesc(reportType.getDescription());
        }
        
        // 设置审核状态描述
        ReportStatus status = ReportStatus.fromCode(report.getStatus());
        if (status != null) {
            vo.setStatusDesc(status.getDescription());
        }
        
        // 查询患者姓名(使用userId)
        if (report.getPatientId() != null) {
            try {
                PatientVO patient = userIdentityClient.getPatientInfo(report.getPatientId());
                if (patient != null) {
                    vo.setPatientName(patient.getNickname());
                }
            } catch (Exception e) {
                log.warn("Failed to get patient name, patientId: {}", report.getPatientId(), e);
            }
        }
        
        // 查询医生姓名(使用userId)
        if (report.getAttendingDoctorId() != null) {
            try {
                com.zixin.accountapi.vo.DoctorVO doctor = userIdentityClient.getDoctorInfo(report.getAttendingDoctorId());
                if (doctor != null) {
                    vo.setDoctorName(doctor.getNickname());
                }
            } catch (Exception e) {
                log.warn("Failed to get doctor name, doctorId: {}", report.getAttendingDoctorId(), e);
            }
        }
        
        return vo;
    }

    private ScheduleVO buildScheduleVO(PatientVO patient, DoctorVO doctor,
                                       ScheduleStatus status,
                                       SchedulePriority priority,
                                       Integer category,
                                       String categoryName,
                                       String link) {
        ScheduleVO scheduleVO = new ScheduleVO();

        // 基础信息
        scheduleVO.setSchedule("查看患者 " + patient.getUsername() + " 的诊断报告");
        scheduleVO.setDoctorId(doctor.getUserId());
        scheduleVO.setDoctorName(doctor.getUsername());
        scheduleVO.setPatientId(patient.getUserId());
        scheduleVO.setPatientName(patient.getUsername());

        // 状态设置
        scheduleVO.setStatus(status.getCode());
        scheduleVO.setStatusDesc(status.getDescription());

        // 优先级设置
        scheduleVO.setPriority(priority.getCode());
        scheduleVO.setPriorityDesc(priority.getDescription());

        // 分类设置
        scheduleVO.setScheduleCategory(category);
        scheduleVO.setScheduleCategoryName(categoryName);

        scheduleVO.setLink(link);

        // 时间设置（默认今天0点到明天0点）
        setDefaultTimeRange(scheduleVO);

        return scheduleVO;
    }

    private void setDefaultTimeRange(ScheduleVO scheduleVO) {
        LocalDate today = LocalDate.now();
        scheduleVO.setScheduleDay(today.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        scheduleVO.setStartTime(today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        scheduleVO.setEndTime(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
}
