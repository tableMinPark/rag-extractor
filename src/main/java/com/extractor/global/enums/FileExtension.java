package com.extractor.global.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileExtension {

    HWP("hwp"),
    HWPX("hwpx"),
    PDF("pdf"),
    XML("xml"),
    ;

    private final String simpleExtension;

    public static FileExtension find(String extension) {

        for (FileExtension ext : FileExtension.values()) {
            if (extension != null && extension.endsWith(ext.simpleExtension)) {
                return ext;
            }
        }

        throw new IllegalArgumentException("No enum constant with value " + extension);
    }
}
