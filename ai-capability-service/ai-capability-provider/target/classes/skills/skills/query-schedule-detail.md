---
name: query-schedule-detail
description: 查询指定医生在某天的详细排班信息，包括具体时间段、日程状态等。当需要了解某医生的具体时间安排、选择合适的预约时间段时使用。
tools:
  - queryDoctorScheduleDetailForDay
---

# 查询详细排班信息

## 指令

当需要获取医生详细时间段时，请执行以下操作：

1. **确定目标医生**：从 `query-doctor-availability` 的返回结果中选择候选医生，获取 `doctorId`。
2. **调用工具**：使用 `queryDoctorScheduleDetailForDay` 工具获取该医生的详细排班。
3. **分析时间段**：关注以下字段：
   - `status`：日程状态，优先选择 `PENDING` 状态
   - `startTime` / `endTime`：时间段，避免冲突
   - `scheduleCategory`：日程类别
4. **选择合适时段**：优先选择 PENDING 状态的时间段，上午时段优先。

## 工具调用

### queryDoctorScheduleDetailForDay

**参数**：

| 参数名 | 类型 | 必填 | 描述 |
|-------|------|------|------|
| doctorId | long | 是 | 医生ID，从概览结果中获取 |
| scheduleDay | string | 是 | 日期格式：YYYY-MM-DD |

**返回示例**：

```json
[
  {
    "id": 10001,
    "doctorName": "张医生",
    "schedule": "上午门诊",
    "scheduleCategory": "OUTPATIENT",
    "scheduleDay": "2024-01-15",
    "status": "PENDING",
    "startTime": "09:00",
    "endTime": "10:00"
  }
]
```

## 日程状态说明

| 状态 | 描述 | 可用性 |
|------|------|--------|
| PENDING | 待处理 | ✅ 可安排新预约 |
| IN_PROGRESS | 进行中 | ❌ 当前时段已被占用 |
| COMPLETED | 已完成 | 可参考历史数据 |

## 日程类别说明

| 类别代码 | 类别名称 | 说明 |
|---------|---------|------|
| OUTPATIENT | 门诊 | 常规门诊服务 |
| FOLLOW_UP | 复诊 | 慢病复诊安排 |
| CONSULTATION | 会诊 | 多学科会诊 |

## 时间段选择策略

```
优先级排序：
1. PENDING 状态的时间段
2. 上午时段优先（一般情况）
3. 考虑患者的特殊时间需求
```

## 注意事项

- 返回的是该医生当日的所有日程记录
- 优先选择 PENDING 状态的时间段
- 注意 startTime 和 endTime，避免时间冲突
- 如果返回空列表，说明该医生当日没有排班记录
