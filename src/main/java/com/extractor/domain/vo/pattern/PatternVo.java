package com.extractor.domain.vo.pattern;

import lombok.Getter;

@Getter
public class PatternVo {

    private final int depth;

    private final String prefix;

    public PatternVo(int depth, String prefix) {
        this.depth = depth;
        this.prefix = prefix;
    }
}
