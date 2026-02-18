package com.zixin.messageapi.api;

import com.zixin.messageapi.dto.SendMessageRequest;
import com.zixin.messageapi.dto.SendMessageResponse;

/**
 * @author yanzixin
 * Created on 2026-02-03
 * To realize is to suffer, But I am still here
 */
public interface SMSAPI {

    SendMessageResponse sendSMSMessage(Long senderId, SendMessageRequest request);

    SendMessageResponse broadcastSMSMessage(Long senderId, SendMessageRequest request);
}
