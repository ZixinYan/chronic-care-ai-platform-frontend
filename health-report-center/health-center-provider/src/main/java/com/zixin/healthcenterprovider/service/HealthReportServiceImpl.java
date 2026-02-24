package com.zixin.healthcenterprovider.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zixin.accountapi.vo.PatientVO;
import com.zixin.healthcenterapi.api.HealthReportAPI;
import com.zixin.healthcenterapi.dto.*;
import com.zixin.healthcenterapi.enums.ReportStatus;
import com.zixin.healthcenterapi.enums.ReportType;
import com.zixin.healthcenterapi.po.HealthReport;
import com.zixin.healthcenterapi.vo.HealthReportVO;
import com.zixin.healthcenterprovider.client.MessageClient;
import com.zixin.healthcenterprovider.client.OSSClient;
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
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
    private final OSSClient ossClient;
    private final UserIdentityClient userIdentityClient;
    
    public HealthReportServiceImpl(HealthReportMapper healthReportMapper, 
                                   MessageClient messageClient,
                                   OSSClient ossClient,
                                   UserIdentityClient userIdentityClient) {
        this.healthReportMapper = healthReportMapper;
        this.messageClient = messageClient;
        this.ossClient = ossClient;
        this.userIdentityClient = userIdentityClient;
    }
    
    @Override
    public UploadReportResponse uploadReport(UploadReportRequest request) {
        StopWatch sw = new StopWatch("uploadReport");
        sw.start("参数校验");
        
        UploadReportResponse response = new UploadReportResponse();
        
        try {
            // 1. 参数校验
            if (request.getPatientId() == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("患者ID不能为空");
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
            
            // 2. 获取患者信息(包含主治医生ID)
            sw.start("查询患者信息");
            PatientVO patient = userIdentityClient.getPatientInfo(request.getPatientId());
            
            if (patient == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("患者信息查询失败");
                return response;
            }
            
            sw.stop();
            log.info("uploadReport - 患者信息查询成功, userId: {}, patientName: {}, attendingDoctorId: {}", 
                    patient.getUserId(), patient.getNickname(), patient.getAttendingDoctorId());
            
            // 3. 处理文件上传(图片或PDF类型)
            String fileUrl = null;
            if (reportType == ReportType.IMAGE || reportType == ReportType.PDF) {
                sw.start("文件上传OSS");
                
                if (request.getFile() == null || request.getFile().isEmpty()) {
                    response.setCode(ToBCodeEnum.FAIL);
                    response.setMessage("报告文件不能为空");
                    return response;
                }
                
                // 调用OSS上传服务
                fileUrl = ossClient.uploadFile(request.getFile());
                
                if (fileUrl == null || fileUrl.isEmpty()) {
                    response.setCode(ToBCodeEnum.FAIL);
                    response.setMessage("文件上传失败");
                    return response;
                }
                
                sw.stop();
                log.info("uploadReport - 文件上传成功, fileUrl: {}", fileUrl);
            }
            
            // 4. 文字类型报告校验
            if (reportType == ReportType.TEXT) {
                if (request.getTextContent() == null || request.getTextContent().trim().isEmpty()) {
                    response.setCode(ToBCodeEnum.FAIL);
                    response.setMessage("文字报告内容不能为空");
                    return response;
                }
            }
            
            // 5. 构建报告实体
            sw.start("保存报告数据");
            HealthReport report = new HealthReport();
            report.setPatientId(request.getPatientId());
            report.setAttendingDoctorId(patient.getAttendingDoctorId());
            report.setReportType(request.getReportType());
            report.setCategory(request.getCategory());
            report.setTitle(request.getTitle());
            report.setDescription(request.getDescription());
            report.setFileUrl(fileUrl);
            report.setTextContent(request.getTextContent());
            
            // 解析报告日期(Unix毫秒时间戳)
            if (request.getReportDate() != null && !request.getReportDate().isEmpty()) {
                try {
                    long reportDate = DateUtil.parseDate(request.getReportDate()).getTime();
                    report.setReportDate(reportDate);
                } catch (Exception e) {
                    log.warn("uploadReport - 报告日期解析失败, reportDate: {}", request.getReportDate());
                }
            }
            
            report.setUploaderId(request.getUploaderId());
            report.setUploaderName(patient.getNickname());
            report.setHospitalName(request.getHospitalName());
            report.setStatus(ReportStatus.PENDING.getCode());
            
            // 6. 保存到数据库
            int rows = healthReportMapper.insert(report);
            sw.stop();

            // 7. 异步操作执行通知
            if (rows > 0 && patient.getAttendingDoctorId() != null) {
                // 发送站内信通知主治医生
                messageClient.sendMessageAsync(
                        patient.getUserId(),
                        SendMessageRequest.builder()
                                .receiverId(patient.getAttendingDoctorId())
                                .messageType(MessageType.SYSTEM.getCode())
                                .title("新健康报告上传通知")
                                .content("患者 " + patient.getNickname() + " 上传了新的健康报告，请及时查看。")
                                .build()
                );
                response.setCode(ToBCodeEnum.SUCCESS);
                response.setMessage("报告上传成功");
                response.setReportId(report.getReportId());
                response.setFileUrl(fileUrl);
                // TODO 自动添加医生工作台

                log.info("uploadReport - 报告上传成功, reportId: {}, patientId: {}, 耗时统计: {}",
                        report.getReportId(), request.getPatientId(), sw.prettyPrint());
            } else {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("报告保存失败");
                log.error("uploadReport - 报告保存失败, patientId: {}", request.getPatientId());
            }
            
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
}
