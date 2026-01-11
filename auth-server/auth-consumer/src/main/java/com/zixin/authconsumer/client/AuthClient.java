package com.zixin.authconsumer.client;

import com.zixin.authprovider.service.JwtServiceImpl;
import dto.GenTokenRequest;
import dto.GenTokenResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class AuthClient {
    @DubboReference
    private JwtServiceImpl jwtService;

    public GenTokenResponse getToken(GenTokenRequest genTokenRequest) {
        return jwtService.genToken(genTokenRequest);
    }
}
