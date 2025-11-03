package com.extractor.global.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChunkType {

    EQUALS("equals"),
    REGEX("regex"),
    NORMAL("normal"),
    TOKEN("token"),
    ;

    private final String code;


    public static ChunkType find(String code) {
        for (ChunkType chunkType : ChunkType.values()) {
            if (code != null && code.equals(ChunkType.NORMAL.code)) continue;
            if (code != null && code.equals(chunkType.code)) {
                return chunkType;
            }
        }
        throw new IllegalArgumentException("No enum constant with value " + code);
    }
}
