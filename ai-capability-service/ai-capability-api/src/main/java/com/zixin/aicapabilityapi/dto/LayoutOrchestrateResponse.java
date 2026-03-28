package com.zixin.aicapabilityapi.dto;

import com.zixin.aicapabilityapi.vo.LayoutBlockVO;
import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 智能排版编排响应。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LayoutOrchestrateResponse extends BaseResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编排后的内容块
     */
    private List<LayoutBlockVO> blocks;

    /**
     * 编排说明
     */
    private String rationale;

    /**
     * 模型原始输出（解析失败时便于排查）
     */
    private String rawModelOutput;
}
