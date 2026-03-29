package com.zixin.accountapi.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum Action {

    READ(1, "可读"),
    WRITE(2, "可编辑"),
    ALL(3, "拥有全部权限");

    private static final Map<Integer, Action> CODE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(Action::getCode, a -> a));

    private final int code;
    private final String desc;

    Action(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Action fromCode(Integer code) {
        Action action = CODE_MAP.get(code);
        if (action == null) {
            throw new IllegalArgumentException("Invalid Action code: " + code);
        }
        return action;
    }
}
