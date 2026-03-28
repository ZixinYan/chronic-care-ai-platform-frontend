package com.zixin.aicapabilityapi.api;

import com.zixin.aicapabilityapi.dto.LayoutOrchestrateRequest;
import com.zixin.aicapabilityapi.dto.LayoutOrchestrateResponse;

/**
 * AI 智能排版编排 Dubbo 接口。
 */
public interface AILayoutAPI {

    /**
     * 根据原始内容与约束生成结构化排版编排结果。
     *
     * @param request 请求参数
     * @return 编排结果
     */
    LayoutOrchestrateResponse orchestrateLayout(LayoutOrchestrateRequest request);
}
