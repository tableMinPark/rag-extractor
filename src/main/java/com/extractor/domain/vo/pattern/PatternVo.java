package com.extractor.domain.vo.pattern;

import lombok.Getter;

import java.util.List;

@Getter
public class PatternVo {

    private final int tokenSize;

    private final List<String> prefixes;

    public PatternVo(int tokenSize, List<String> prefixes) {
        this.tokenSize = tokenSize;
        this.prefixes = prefixes;
    }
}
