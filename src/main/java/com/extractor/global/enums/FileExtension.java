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
    DATABASE("DATABASE"),
    ;

    private final String simpleExtension;

    public static FileExtension find(String extension) {

        for (FileExtension ext : FileExtension.values()) {
            if (ext.equals(FileExtension.DATABASE)) continue;
            if (extension != null && extension.endsWith(ext.simpleExtension)) {
                return ext;
            }
        }

        throw new IllegalArgumentException("No enum constant with value " + extension);
    }
}
