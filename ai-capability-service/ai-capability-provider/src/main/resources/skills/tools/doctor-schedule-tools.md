# 医生排班工具集

## 工具定义

### queryDoctorAvailabilityForDay

```json
{
  "name": "queryDoctorAvailabilityForDay",
  "description": "查询指定日期所有有排班记录的医生出勤情况与画像信息。返回包含医生ID、姓名、科室、职称、工作经验及当日负载情况的JSON数据。请假医生会被自动过滤。",
  "parameters": {
    "type": "object",
    "properties": {
      "scheduleDay": {
        "type": "string",
        "description": "预约日期，格式为 YYYY-MM-DD",
        "pattern": "^\\d{4}-\\d{2}-\\d{2}$"
      },
      "businessRequirement": {
        "type": "string",
        "description": "业务需求描述，例如 '内科门诊'、'复诊安排'"
      }
    },
    "required": ["scheduleDay"]
  }
}
```

**返回示例**：

```json
{
  "scheduleDay": "2024-01-15",
  "businessRequirement": "内科门诊",
  "doctors": [
    {
      "doctorId": 1001,
      "doctorName": "张医生",
      "department": "内科",
      "title": "主任医师",
      "experience": 15,
      "totalSchedules": 8,
      "pendingSchedules": 3,
      "inProgressSchedules": 2
    }
  ]
}
```

---

### queryDoctorScheduleDetailForDay

```json
{
  "name": "queryDoctorScheduleDetailForDay",
  "description": "查询指定医生在某天的详细排班信息，包括具体时间段、日程状态等。用于选择合适的预约时间段。",
  "parameters": {
    "type": "object",
    "properties": {
      "doctorId": {
        "type": "integer",
        "description": "医生ID（账户ID）"
      },
      "scheduleDay": {
        "type": "string",
        "description": "预约日期，格式为 YYYY-MM-DD",
        "pattern": "^\\d{4}-\\d{2}-\\d{2}$"
      }
    },
    "required": ["doctorId", "scheduleDay"]
  }
}
```

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

---

## 调用流程

```
┌─────────────────────────────────────────────────────────────┐
│ Step 1: 获取医生概览                                         │
│                                                             │
│ queryDoctorAvailabilityForDay(scheduleDay, businessReq)    │
│                                                             │
│ 返回: 医生列表（含画像、负载信息）                            │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 2: 筛选候选医生                                         │
│                                                             │
│ 根据科室、职称、经验、负载筛选出 2-3 名候选医生               │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 3: 获取详情（可选）                                      │
│                                                             │
│ queryDoctorScheduleDetailForDay(doctorId, scheduleDay)     │
│                                                             │
│ 返回: 该医生的详细时间段列表                                  │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 4: 生成推荐                                             │
│                                                             │
│ 综合分析，输出最终推荐结果（JSON格式）                        │
└─────────────────────────────────────────────────────────────┘
```

## 数据来源

| 数据类型 | 来源服务 | 字段 |
|---------|---------|------|
| 医生画像 | UserIdentityAPI | 姓名、科室、职称、工作经验 |
| 排班数据 | DoctorWorkbenchAPI | 日程记录、时间段、状态 |
| 请假数据 | 内部处理 | 自动过滤已请假医生 |

## 技术参数

| 参数 | 值 | 说明 |
|------|-----|------|
| 超时时间 | 50 秒 | Dubbo RPC 调用超时 |
| 分页大小 | 1000 | 单次查询最大返回数 |
| 数据格式 | JSON | 所有返回均为 JSON 字符串 |

## 错误处理

### 参数校验错误

```json
{
  "error": "scheduleDay is required"
}
```

### 服务调用异常

```json
{
  "scheduleDay": "2024-01-15",
  "businessRequirement": "内科门诊",
  "doctors": []
}
```

**注意**：即使发生异常，工具也会返回有效 JSON，不会抛出错误导致流程中断。

## 最佳实践

### ✅ DO

- 先调用 `queryDoctorAvailabilityForDay` 获取概览
- 根据返回数据进行分析和筛选
- 必要时再调用 `queryDoctorScheduleDetailForDay` 获取详情
- 基于真实数据生成推荐

### ❌ DON'T

- 不要在没有调用工具的情况下凭空推荐
- 不要忽略返回的负载信息
- 不要推荐过载的医生
- 不要推荐请假中的医生（已被过滤）

## 版本信息

- **版本**：v1.0
- **更新时间**：2024-01
- **维护团队**：AI 能力服务组
