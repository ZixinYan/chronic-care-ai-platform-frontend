package com.zixin.aicapabilityapi.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 智能排版编排请求。
 */
@Data
public class LayoutOrchestrateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待编排的原始内容（必填）
     */
    private String sourceContent;

    /**
     * 内容类型，如 REPORT、TEXT、SUMMARY
     */
    private String contentType;

    /**
     * 目标结构形式提示，如 JSON、MARKDOWN
     */
    private String targetFormat;

    /**
     * 用户补充约束或偏好说明
     */
    private String constraints;
}
