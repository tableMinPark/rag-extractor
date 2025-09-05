package com.extractor.domain.vo.pattern;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PatternVo {

    private final int tokenSize;

    private final List<PrefixVo> prefixes;

    @Builder
    public PatternVo(int tokenSize, List<PrefixVo> prefixes) {
        this.tokenSize = tokenSize;
        this.prefixes = prefixes;
    }
}
