package com.extractor.application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SourceType {

    FILE("SOURCE-TYPE-FILE"),
    REPO("SOURCE-TYPE-REPO"),
    ;

    private final String code;
}
