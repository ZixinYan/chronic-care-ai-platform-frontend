---
name: query-doctor-availability
description: 查询指定日期所有有排班记录的医生出勤情况与画像信息。当需要获取某日所有候选医生概览、评估医生负载、或健康报告上传需分配审核医生时使用。触发词：排班、医生列表、出勤、负载、queryDoctorAvailabilityForDay。
---

# 查询医生出勤情况

## 指令

当需要获取医生排班概览时，请执行以下操作：

1. **确认日期参数**：用户可能给出 `yyyyMMdd` 或 `YYYY-MM-DD`；**调用工具时**请使用用户在提示中给出的「工具用 scheduleDay」一行（已为 YYYY-MM-DD）；若仅见八位数字，先换为 `YYYY-MM-DD` 再调用工具。
2. **确认业务需求**：提取 `businessRequirement`（可选），用于筛选匹配的医生。
3. **调用工具**：使用已注册的 Spring 工具 **`queryDoctorAvailabilityForDay`**（参数 scheduleDay, businessRequirement）。
4. **分析返回结果**：解析医生列表，关注以下字段：
   - `department`：科室，用于匹配业务需求
   - `title`：职称，主任医师优先级最高
   - `experience`：工作经验年限
   - `pendingSchedules` + `inProgressSchedules`：当日负载
5. **筛选候选医生**：根据科室匹配、职称、负载情况筛选出 2-3 名候选医生。

## 工具 queryDoctorAvailabilityForDay

| 参数名 | 类型 | 必填 | 描述 |
|-------|------|------|------|
| scheduleDay | string | 是 | YYYY-MM-DD（与医生库 schedule_day 一致；工具内部也会归一化八位日期） |
| businessRequirement | string | 否 | 业务需求描述 |

## 后续技能联动

- 获取概览后，可调用 **query-schedule-detail** 查询某医生的详细排班
- 数据分析完成后，按 **recommend-schedule** 输出最终 JSON
