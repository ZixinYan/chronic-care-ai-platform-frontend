# query-doctor-availability

## Skill Definition

```json
{
  "name": "query-doctor-availability",
  "description": "查询指定日期所有有排班记录的医生出勤情况与画像信息，用于智能排班推荐。返回包含医生ID、姓名、科室、职称、工作经验及当日负载情况的JSON数据。",
  "parameters": {
    "type": "object",
    "properties": {
      "scheduleDay": {
        "type": "string",
        "description": "预约日期，格式为 YYYY-MM-DD，例如 2024-01-15",
        "pattern": "^\\d{4}-\\d{2}-\\d{2}$"
      },
      "businessRequirement": {
        "type": "string",
        "description": "业务需求描述，例如 '内科门诊'、'复诊安排'，用于筛选匹配的医生"
      }
    },
    "required": ["scheduleDay"]
  },
  "tools": [
    {
      "name": "queryDoctorAvailabilityForDay",
      "description": "查询指定日期所有医生的出勤情况与画像信息"
    }
  ]
}
```

## 触发条件

- 需要获取某日所有候选医生概览
- 需要评估医生的当日负载情况
- 需要获取医生的画像信息（科室、职称、工作经验）

## 工具调用

### queryDoctorAvailabilityForDay

**描述**：查询指定日期所有医生的出勤情况与画像信息

**参数**：

| 参数名 | 类型 | 必填 | 描述 |
|-------|------|------|------|
| scheduleDay | string | 是 | 日期格式：YYYY-MM-DD |
| businessRequirement | string | 否 | 业务需求描述 |

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
    },
    {
      "doctorId": 1002,
      "doctorName": "李医生",
      "department": "内科",
      "title": "副主任医师",
      "experience": 10,
      "totalSchedules": 6,
      "pendingSchedules": 2,
      "inProgressSchedules": 1
    }
  ]
}
```

## 返回字段说明

| 字段 | 类型 | 描述 |
|------|------|------|
| doctorId | long | 医生ID（账户ID） |
| doctorName | string | 医生姓名 |
| department | string | 所属科室 |
| title | string | 职称（主任医师/副主任医师/主治医师/住院医师） |
| experience | integer | 工作经验年限 |
| totalSchedules | integer | 当日总排班数 |
| pendingSchedules | integer | 待处理的排班数 |
| inProgressSchedules | integer | 进行中的排班数 |

## 决策逻辑

获取数据后，按以下维度筛选医生：

| 筛选维度 | 优先级 | 说明 |
|---------|--------|------|
| 科室匹配 | 高 | 优先选择科室匹配的医生 |
| 职称 | 中 | 主任医师 > 副主任医师 > 主治医师 |
| 负载 | 中 | 选择负载适中（4-6）的医生 |
| 经验 | 低 | 经验年限越长优先级越高 |

## 注意事项

- 返回的医生列表已按医生ID聚合
- 请假医生会被自动过滤，不会出现在结果中
- 如果某医生没有排班记录，不会出现在结果中

## 后续技能联动

- 获取概览后，可调用 `query-schedule-detail` 查询某医生的详细排班
- 数据分析完成后，调用 `recommend-schedule` 生成最终推荐
