package com.extractor.global.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SourceType {

    FILE("SOURCE-TYPE-FILE"),
    DB("SOURCE-TYPE-DB"),
    ;

    private final String code;
}
