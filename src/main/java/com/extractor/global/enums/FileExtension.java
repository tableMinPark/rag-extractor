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

    private final String ext;

    public static FileExtension find(String extension) {

        for (FileExtension fileExtension : FileExtension.values()) {
            if (extension != null && extension.endsWith(fileExtension.ext)) {
                return fileExtension;
            }
        }

        throw new IllegalArgumentException("No enum constant with value " + extension);
    }
}
