package com.zixin.authprovider.service;

import com.zixin.authapi.api.JwtAPI;
import com.zixin.authprovider.utils.JwtUtils;
import com.zixin.utils.exception.ToBCodeEnum;
import dto.GenTokenRequest;
import dto.GenTokenResponse;
import dto.RefreshTokenRequest;
import dto.RefreshTokenResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@Slf4j
public class JwtServiceImpl implements JwtAPI {

    private final JwtUtils jwtUtils;

    public JwtServiceImpl(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public GenTokenResponse genToken(GenTokenRequest request) {
        GenTokenResponse response = new GenTokenResponse();

        if (request.getUserId() == null) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("userId is required");
            log.error("Invalid request data: {}", request);
            return response;
        }

        try {
            String token = jwtUtils.generateToken(request.getUserId());
            if (token == null || token.isEmpty()) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("Token generation failed");
                log.error("Token is empty for request: {}", request);
                return response;
            }

            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("Token generated successfully");
            response.setToken(token);
            log.info("Token generated successfully for userId: {}", request.getUserId());

        } catch (Exception e) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("Token generation failed");
            log.error("Token generation exception", e);
        }

        return response;
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshTokenResponse response = new RefreshTokenResponse();


        return response;
    }
}
