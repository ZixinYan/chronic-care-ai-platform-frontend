---
name: query-doctor-availability
description: 查询指定日期所有有排班记录的医生出勤情况与画像信息。当需要获取某日所有候选医生概览、评估医生负载情况时使用。
tools:
  - queryDoctorAvailabilityForDay
---

# 查询医生出勤情况

## 指令

当需要获取医生排班概览时，请执行以下操作：

1. **确认日期参数**：从用户请求中提取 `scheduleDay`（格式：YYYY-MM-DD）。
2. **确认业务需求**：提取 `businessRequirement`（可选），用于筛选匹配的医生。
3. **调用工具**：使用 `queryDoctorAvailabilityForDay` 工具获取数据。
4. **分析返回结果**：解析医生列表，关注以下字段：
   - `department`：科室，用于匹配业务需求
   - `title`：职称，主任医师优先级最高
   - `experience`：工作经验年限
   - `pendingSchedules` + `inProgressSchedules`：当日负载
5. **筛选候选医生**：根据科室匹配、职称、负载情况筛选出 2-3 名候选医生。

## 工具调用

### queryDoctorAvailabilityForDay

**参数**：

| 参数名 | 类型 | 必填 | 描述 |
|-------|------|------|------|
| scheduleDay | string | 是 | 日期格式：YYYY-MM-DD |
| businessRequirement | string | 否 | 业务需求描述，如 "内科门诊" |

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

## 决策逻辑

获取数据后，按以下维度筛选医生：

| 筛选维度 | 优先级 | 说明 |
|---------|--------|------|
| 科室匹配 | 高 | 优先选择科室匹配的医生 |
| 职称 | 中 | 主任医师 > 副主任医师 > 主治医师 |
| 负载 | 中 | 选择负载适中（4-6）的医生 |
| 经验 | 低 | 经验年限越长优先级越高 |

## 后续技能联动

- 获取概览后，可调用 `query-schedule-detail` 查询某医生的详细排班
- 数据分析完成后，调用 `recommend-schedule` 生成最终推荐

## 注意事项

- 返回的医生列表已按医生ID聚合
- 请假医生会被自动过滤，不会出现在结果中
- 如果某医生没有排班记录，不会出现在结果中
