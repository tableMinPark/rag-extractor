package com.document.extractor.application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UpdateState {

    STAY("UPDATE-STATE-STAY"),
    INSERT("UPDATE-STATE-INSERT"),
    CHANGE("UPDATE-STATE-CHANGE"),
    DELETE("UPDATE-STATE-DELETE"),
    ;

    private final String code;

    public static UpdateState find(String code) {
        for (UpdateState updateState : UpdateState.values()) {
            if (code != null && code.equalsIgnoreCase(updateState.code)) {
                return updateState;
            }
        }

        throw new IllegalArgumentException("No enum constant with value " + code);
    }
}
