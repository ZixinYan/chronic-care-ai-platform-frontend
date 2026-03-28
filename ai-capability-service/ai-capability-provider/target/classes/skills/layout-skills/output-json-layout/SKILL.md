---
name: output-json-layout
description: 将已定稿的排版结构格式化为 API 所需的纯 JSON。在结构已确定、需要返回结果时使用。
---

# 输出 JSON 排版结果

## 指令

1. 确认 `blocks` 已按阅读顺序设置 `order`（1..n）。
2. `level`：1=文档主章，2=子节，3=更细条目；保持层级合理。
3. `styleHint`：可选用 `emphasis`、`callout`、`table`、`list` 等简短标记，无则置空字符串。
4. **只输出一个 JSON 对象**，键为 `blocks` 与 `rationale`，不要用 Markdown 代码块包裹。

## 示例（结构示意）

{"blocks":[{"order":1,"level":1,"title":"概述","body":"……","styleHint":""}],"rationale":"按时间线重组并突出结论"}
