package com.zixin.authconsumer.controller;

import com.zixin.accountapi.dto.LoginRequest;
import com.zixin.authconsumer.service.SSOServiceImpl;
import com.zixin.thirdpartyapi.dto.SendSMSRequest;
import com.zixin.utils.utils.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth/sso")
public class SSOController {

    private final SSOServiceImpl ssoService;

    public SSOController(SSOServiceImpl ssoService) {
        this.ssoService = ssoService;
    }

    @RequestMapping("/login")
    public Result login(@RequestParam LoginRequest loginRequest) {
        return ssoService.login(loginRequest);
    }

    @RequestMapping("/register")
    public Result register(@RequestParam LoginRequest registerRequest) {
        return ssoService.register(registerRequest);
    }

    @RequestMapping("/send/SMS/code")
    public Result sendSmsCode(@RequestParam SendSMSRequest sendSMSRequest) {
        return ssoService.sendCode(sendSMSRequest);
    }
}
