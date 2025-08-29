package com.extractor.global.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileExtension {

    HWP("application/vnd.hancom.hwp"),
    HWPX("application/vnd.hancom.hwpx"),
    PDF("application/pdf"),
    XML("application/xml"),
    ;

    private final String extension;

    public boolean isEquals(String extension) {
        return this.extension.equals(extension);
    }
}
