---
name: recommend-schedule
description: 在已通过工具拿到真实医生与排班数据后，生成最终 JSON 推荐。健康报告等业务要求 schedules 中必须含 doctorId（与工具返回一致）。触发词：最终推荐、输出 JSON、排班结果。
---

# 生成排班推荐

## 指令

1. **确认数据完整性**：必须先调用 `queryDoctorAvailabilityForDay`（及必要时 `queryDoctorScheduleDetailForDay`）拿到真实数据。
2. **应用决策原则**：科室匹配、职称权重、负载均衡（4-6 待处理优先）、时间段 PENDING 优先。
3. **输出**：仅输出**纯 JSON**，无 Markdown 代码块。
4. **必填字段**：每条 `schedules[]` 必须含 **`doctorId`**（Long，等于工具 JSON 中的 `doctors[].doctorId`），禁止编造。`startTime`/`endTime` 用 Unix 毫秒 Long 或 `null`，禁止 `"09:00"` 字符串。

## 输出示例

```json
{
  "schedules": [
    {
      "id": null,
      "doctorId": 1001,
      "doctorName": "张医生",
      "patientName": null,
      "schedule": "上午门诊",
      "scheduleCategory": null,
      "scheduleCategoryName": null,
      "scheduleDay": "2024-01-15",
      "priority": null,
      "priorityDesc": null,
      "status": null,
      "statusDesc": null,
      "result": null,
      "link": null,
      "startTime": null,
      "endTime": null
    }
  ],
  "recommendation": "推荐理由（中文，具体说明科室/职称/负载/时段）"
}
```

## 质量检查

- [ ] `doctorId` 来自工具返回值
- [ ] `scheduleDay` 为 YYYY-MM-DD
- [ ] JSON 可被严格解析
