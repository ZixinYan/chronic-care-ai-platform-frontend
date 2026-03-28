package com.zixin.aicapabilityapi.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 智能排版编排中的单个内容块。
 */
@Data
public class LayoutBlockVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 展示顺序，从 1 递增
     */
    private Integer order;

    /**
     * 标题层级，1 表示最高级章节
     */
    private Integer level;

    /**
     * 块标题
     */
    private String title;

    /**
     * 块正文（可为 Markdown 或纯文本）
     */
    private String body;

    /**
     * 排版或样式提示（如 emphasis、callout）
     */
    private String styleHint;
}
