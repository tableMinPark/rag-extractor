package com.document.extractor.application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SourceType {

    FILE("SOURCE-TYPE-FILE"),
    REPO("SOURCE-TYPE-REPO"),
    ;

    private final String code;

    public static SourceType find(String code) {
        for (SourceType sourceType : SourceType.values()) {
            if (code != null && code.equalsIgnoreCase(sourceType.code)) {
                return sourceType;
            }
        }

        throw new IllegalArgumentException("No enum constant with value " + code);
    }
}
