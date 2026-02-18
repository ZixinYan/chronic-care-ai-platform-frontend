package com.zixin.accountapi.vo;

import lombok.Data;
import java.util.Set;

@Data
public class UserAuthority {
    private Long userId;
    /** 形如 DOCTOR:READ */
    private Set<String> authorities;
}
