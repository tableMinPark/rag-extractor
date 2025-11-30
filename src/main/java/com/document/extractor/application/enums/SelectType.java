package com.document.extractor.application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SelectType {

    NONE("none"),
    REGEX("regex"),
    TOKEN("token"),
    ;

    private final String code;

    public static SelectType find(String code) {
        for (SelectType selectType : SelectType.values()) {
            if (code != null && code.equals(selectType.code)) {
                return selectType;
            }
        }

        throw new IllegalArgumentException("No enum constant with value " + code);
    }
}
