package com.extractor.domain.vo.pattern;

import lombok.Getter;

import java.util.List;

@Getter
public class ChunkPatternVo {

    private final List<PatternVo> patterns;

    private final List<String> stopPatterns;

    public ChunkPatternVo(List<PatternVo> patterns, List<String> stopPatterns) {
        this.patterns = patterns;
        this.stopPatterns = stopPatterns;
    }
}
