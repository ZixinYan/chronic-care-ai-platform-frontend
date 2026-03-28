package com.zixin.aicapabilityapi.dto;

import com.zixin.aicapabilityapi.vo.LayoutBlockVO;
import lombok.Data;

import java.util.List;

/**
 * 大模型输出的排版编排 JSON 对应的强类型结构，供 Gson 反序列化。
 */
@Data
public class AiLayoutResult {

    /**
     * 编排后的内容块列表
     */
    private List<LayoutBlockVO> blocks;

    /**
     * 编排说明或设计理由
     */
    private String rationale;
}
