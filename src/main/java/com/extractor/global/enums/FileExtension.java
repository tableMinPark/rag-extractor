package com.extractor.global.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileExtension {

    HWP("application/vnd.hancom.hwp", "hwp"),
    HWPX("application/vnd.hancom.hwpx", "hwpx"),
    PDF("application/pdf", "pdf"),
    XML("application/xml", "xml"),
    ;

    private final String extension;

    private final String simpleExtension;

    public boolean isEquals(String extension) {
        return this.extension.equals(extension);
    }

    public static FileExtension find(String extension) {

        for (FileExtension ext : FileExtension.values()) {
            if (extension != null && extension.endsWith(ext.simpleExtension)) {
                return ext;
            }
        }

        throw new IllegalArgumentException("No enum constant with value " + extension);
    }
}
