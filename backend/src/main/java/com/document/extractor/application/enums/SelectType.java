package com.document.extractor.application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SelectType {

    NONE("SELECT-TYPE-NONE", "미설정"),
    REGEX("SELECT-TYPE-REGEX", "정규식"),
    TOKEN("SELECT-TYPE-TOKEN", "토큰"),
    ;

    private final String code;

    private final String name;

    public static SelectType find(String code) {
        for (SelectType selectType : SelectType.values()) {
            if (code != null && code.equalsIgnoreCase(selectType.code)) {
                return selectType;
            }
        }

        throw new IllegalArgumentException("No enum constant with value " + code);
    }
}
