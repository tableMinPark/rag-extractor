package com.document.extractor.application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExtractType {

    MARK_DOWN("markdown"),
    HTML("html")
    ;

    private final String code;

    public static ExtractType find(String code) {
        for (ExtractType extractType : ExtractType.values()) {
            if (code != null && code.equals(extractType.code)) {
                return extractType;
            }
        }

        throw new IllegalArgumentException("No enum constant with value " + code);
    }
}
