# query-schedule-detail

## Skill Definition

```json
{
  "name": "query-schedule-detail",
  "description": "查询指定医生在某天的详细排班信息，包括具体时间段、日程状态等。用于在获取医生概览后，进一步了解某医生的具体时间安排，以便选择合适的预约时间段。",
  "parameters": {
    "type": "object",
    "properties": {
      "doctorId": {
        "type": "integer",
        "description": "医生ID（账户ID），从 query-doctor-availability 的返回结果中获取"
      },
      "scheduleDay": {
        "type": "string",
        "description": "预约日期，格式为 YYYY-MM-DD",
        "pattern": "^\\d{4}-\\d{2}-\\d{2}$"
      }
    },
    "required": ["doctorId", "scheduleDay"]
  },
  "tools": [
    {
      "name": "queryDoctorScheduleDetailForDay",
      "description": "查询指定医生在某天的详细排班信息"
    }
  ]
}
```

## 触发条件

- 已通过 `query-doctor-availability` 获取医生概览
- 需要了解某医生的具体时间段安排
- 需要选择合适的预约时间段
- 需要确认时间段的可用状态

## 工具调用

### queryDoctorScheduleDetailForDay

**描述**：查询指定医生在某天的详细排班信息

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
    "patientName": null,
    "schedule": "上午门诊",
    "scheduleCategory": "OUTPATIENT",
    "scheduleCategoryName": "门诊",
    "scheduleDay": "2024-01-15",
    "priority": 1,
    "priorityDesc": "高优先级",
    "status": "PENDING",
    "statusDesc": "待处理",
    "result": null,
    "link": null,
    "startTime": "09:00",
    "endTime": "10:00"
  },
  {
    "id": 10002,
    "doctorName": "张医生",
    "patientName": null,
    "schedule": "下午门诊",
    "scheduleCategory": "OUTPATIENT",
    "scheduleCategoryName": "门诊",
    "scheduleDay": "2024-01-15",
    "priority": 2,
    "priorityDesc": "中优先级",
    "status": "IN_PROGRESS",
    "statusDesc": "进行中",
    "result": null,
    "link": null,
    "startTime": "14:00",
    "endTime": "15:00"
  }
]
```

## 返回字段说明

| 字段 | 类型 | 描述 |
|------|------|------|
| id | long | 日程记录ID |
| doctorName | string | 医生姓名 |
| patientName | string | 患者姓名（可能为空） |
| schedule | string | 日程描述 |
| scheduleCategory | string | 日程类别代码 |
| scheduleCategoryName | string | 日程类别名称 |
| scheduleDay | string | 日程日期 |
| priority | integer | 优先级（1-高，2-中，3-低） |
| priorityDesc | string | 优先级描述 |
| status | string | 状态代码 |
| statusDesc | string | 状态描述 |
| startTime | string | 开始时间 |
| endTime | string | 结束时间 |

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
