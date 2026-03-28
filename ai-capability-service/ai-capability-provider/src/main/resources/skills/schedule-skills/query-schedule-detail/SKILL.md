---
name: query-schedule-detail
description: 查询指定医生在某天的详细排班信息（时间段、状态）。当需要为候选医生选具体时段、或验证 PENDING 空档时使用。触发词：详细排班、时间段、queryDoctorScheduleDetailForDay。
---

# 查询详细排班信息

## 指令

1. **确定目标医生**：从 `query-doctor-availability` 的返回结果中选择候选医生，获取 `doctorId`（医生用户 ID）。
2. **调用工具**：使用 **`queryDoctorScheduleDetailForDay`**（参数 doctorId, scheduleDay，日期须与上一步相同口径 YYYY-MM-DD）。
3. **分析时间段**：关注 `status`（优先 PENDING）、`startTime` / `endTime`、`scheduleCategory`。
4. **选择合适时段**：优先 PENDING，上午优先。

## 工具 queryDoctorScheduleDetailForDay

| 参数名 | 类型 | 必填 | 描述 |
|-------|------|------|------|
| doctorId | long | 是 | 医生用户 ID，来自概览 `doctors[].doctorId` |
| scheduleDay | string | 是 | YYYY-MM-DD |

## 日程状态

| 状态 | 可用性 |
|------|--------|
| PENDING | 可安排新预约 |
| IN_PROGRESS | 时段占用 |
