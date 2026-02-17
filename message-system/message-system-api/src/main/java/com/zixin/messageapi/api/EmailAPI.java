package com.zixin.messageapi.api;

import com.zixin.messageapi.dto.SendMessageRequest;
import com.zixin.messageapi.dto.SendMessageResponse;

/**
 * @author yanzixin <yanzixin@kuaishou.com>
 * Created on 2026-02-03
 * To realize is to suffer, But I am still here
 */
public interface EmailAPI {
    SendMessageResponse sendEmailMessage(Long senderId, SendMessageRequest request);

    SendMessageResponse broadcastEmailMessage(Long senderId, SendMessageRequest request);
}
