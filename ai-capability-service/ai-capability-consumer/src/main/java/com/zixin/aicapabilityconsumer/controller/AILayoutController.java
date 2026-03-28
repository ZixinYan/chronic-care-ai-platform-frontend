package com.zixin.aicapabilityconsumer.controller;

import com.zixin.aicapabilityapi.api.AILayoutAPI;
import com.zixin.aicapabilityapi.dto.LayoutOrchestrateRequest;
import com.zixin.aicapabilityapi.dto.LayoutOrchestrateResponse;
import com.zixin.utils.exception.ToBCodeEnum;
import com.zixin.utils.utils.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 智能排版编排 HTTP 入口，便于联调。
 */
@RestController
@RequestMapping("/ai/layout")
public class AILayoutController {

    @DubboReference(check = false)
    private AILayoutAPI aiLayoutAPI;

    /**
     * 提交原始内容与约束，获取结构化排版块列表。
     *
     * @param request 编排请求
     * @return 统一响应包装
     */
    @PostMapping("/orchestrate")
    public Result<?> orchestrate(@RequestBody LayoutOrchestrateRequest request) {
        LayoutOrchestrateResponse response = aiLayoutAPI.orchestrateLayout(request);
        if (ToBCodeEnum.SUCCESS.equals(response.getCode())) {
            return Result.success(response);
        }
        return Result.error(response.getMessage());
    }
}
