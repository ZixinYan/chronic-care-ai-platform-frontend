package com.zixin.authconsumer.client;

import com.zixin.thirdpartyapi.api.SMSAPI;
import com.zixin.thirdpartyapi.dto.SendSMSRequest;
import com.zixin.thirdpartyapi.dto.SendSMSResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class ThirdPartyClient {
    @DubboReference
    private SMSAPI smsServiceImpl;

    public SendSMSResponse sendSMS(SendSMSRequest sendSMSRequest) {
        return smsServiceImpl.sendSMS(sendSMSRequest);
    }
}
