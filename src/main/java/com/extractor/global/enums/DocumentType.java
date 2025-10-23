package com.extractor.global.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentType {

    FILE("DOC-TYPE-FILE"),
    DB("DOC-TYPE-DB"),
    ;

    private final String code;
}
